# EV Carbon Charge Optimizer

Monorepo aplikacji webowej dla kierowców samochodów elektrycznych (zadanie rekrutacyjne **Codibly IT Academy**).  
Backend umożliwia analizę prognozowanego miksu energetycznego Wielkiej Brytanii, wyznaczanie najczystszego okna ładowania oraz trwałe zarządzanie rezerwacjami ładowarek.

---

## Struktura repozytorium

```text
.
├── Backend/        # Java 25 + Spring Boot 4.2.0 (REST API, JPA, Caffeine, Gradle)
├── Frontend/       # Osobna aplikacja kliencka (React + TypeScript)
└── README.md
```

---

## Architektura i wzorce projektowe

Backend został zaprojektowany w duchu **Clean / Hexagonal Architecture** (Ports & Adapters) z wyraźnym kierunkiem zależności `infrastructure` → `application` → `domain`:

* **`domain/`** — czysta Java (zero zależności od Springa, JPA i bibliotek HTTP). Zawiera modele domenowe (`Booking`, `Charger`, `TimeRange`, `GenerationInterval`, `DailyEnergyMix`, `ChargingWindow`), enum `EnergySource` z klasyfikacją czystych źródeł, wyjątki domenowe, interfejsy portów wyjściowych oraz logikę biznesową.
* **`application/`** — przypadki użycia (porty wejściowe `GetEnergyMixUseCase`, `FindBestChargingWindowUseCase`, `CreateBookingUseCase` itd.), niemutowalne rekordy DTO komend i wyników oraz serwisy aplikacyjne orkiestrujące domenę w granicach transakcji (`@Transactional`).
* **`infrastructure/`** — adaptery wejściowe (kontrolery REST, `GlobalExceptionHandler` z `ProblemDetail`, `UserContextResolver`), adaptery wyjściowe (`CarbonIntensityApiClient`, repozytoria JPA, encje, seeder `ChargerDataSeeder`) oraz konfiguracja (`RestClient`, Caffeine, `Clock`, CORS).

Zastosowane wzorce i zasady SOLID:
* **Strategy** (`CleanEnergyCalculationStrategy` / `AvailableIntervalsMeanStrategy`) — enkapsulacja polityki agregacji danych i obsługi niekompletnych przedziałów.
* **Specification** (`BookingSpecification`: `FutureStartTimeSpecification`, `BookingDurationSpecification`, `NoBookingOverlapSpecification`) — modularne, testowalne reguły walidacji rezerwacji.
* **Factory Method** (`Booking.create(...)` i `Booking.reconstitute(...)`) — wymuszenie niezmienników przy tworzeniu nowej rezerwacji przy jednoczesnym bezproblemowym odtwarzaniu historycznych wpisów z bazy.
* **Repository & Adapter** — porty w `domain/port`, implementacje w `infrastructure/adapter/out`.
* **Open/Closed Principle** — źródło energii (`EnergySource.isClean()`) decyduje o czystości bez modyfikowania algorytmów zliczających.

---

## Kluczowe decyzje projektowe

1. **Polityka niepełnych danych z Carbon Intensity API (`AvailableIntervalsMeanStrategy`)**
   * Doba UTC składa się z **48 interwałów** 30-minutowych (`[00:00Z, 24:00Z)`). Po pobraniu danych z API interwały są deduplikowane i precyzyjnie przypisywane do dób po `interval.from()`.
   * Średnie udziały źródeł i czystej energii (`biomass`, `nuclear`, `hydro`, `wind`, `solar`) obliczane są wyłącznie na podstawie **dostępnych** interwałów (aplikacja nie zwraca błędu 500 przy brakach części slotów).
   * Przy pokryciu `>= 90%` (`>= 44` z 48 slotów) dzień ma status `COMPLETE`, poniżej progu `PARTIAL`, a przy braku slotów `UNAVAILABLE` (z udziałami `0.0`).

2. **Ciągła oś czasu i tie-breaker okna ładowania (`ChargingWindowOptimizer`)**
   * Optymalizator operuje na posortowanej ciągłej osi czasu obejmującej **jutro i pojutrze (UTC)** (`96` oczekiwanych slotów), dzięki czemu okno o długości `1–6` godzin (`2–12` kolejnych 30-minutowych interwałów) może swobodnie przechodzić przez północ.
   * Okno jest brane pod uwagę tylko wtedy, gdy `N * 2` slotów tworzy **ciągły przedział bez luk czasowych** (`prev.to() == next.from()`). Jeśli braki danych uniemożliwiają znalezienie ciągłego okna, serwis zwraca kontrolowany błąd **503 Service Unavailable**.
   * **Tie-breaker:** w przypadku remisu średniej czystej energii wybierane jest **wcześniejsze** okno (ostry warunek `>` podczas przeglądu chronologicznego).

3. **Baza danych Supabase: Session Pooler (port 5432) i schemat `ev_charging`**
   * Połączenie JDBC powinno wskazywać na **Session pooler (port `5432`)**, a nie Transaction pooler (`6543`), ponieważ Hibernate korzysta z *prepared statements* wymagających spójnego stanu sesji.
   * Konfiguracja ustawia `spring.jpa.open-in-view=false`, `spring.jpa.hibernate.ddl-auto=update`, `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` oraz dedykowany schemat `ev_charging`.
   * Na tabeli `bookings` zdefiniowano klucz obcy do `chargers` oraz złożony indeks `(charger_id, start_time, end_time)` przyspieszający zapytania o kolizje.

4. **Współbieżność rezerwacji (`PESSIMISTIC_WRITE`)**
   * Zamiast rozszerzenia `btree_gist` po stronie bazy, `BookingApplicationService.createBooking` pobiera rekord ładowarki z blokadą pesymistyczną (`LockModeType.PESSIMISTIC_WRITE`) w ramach transakcji `@Transactional`, serializując równoległe próby rezerwacji tej samej ładowarki i eliminując *race conditions*.
   * Przedziały są traktowane jako półotwarte `[startTime, endTime)`: nakładanie występuje dla `startA < endB && endA > startB`, więc styk `14:00` i `14:00` jest dozwolony. Czas trwania musi wynosić `1–6` pełnych godzin (`duration % 60 min == 0`), z dowolną minutą rozpoczęcia.

5. **Cache’owanie odpowiedzi zewnętrznego API (Caffeine)**
   * Wywołania `CarbonIntensityApiClient` są cache’owane (`generation-mix`, TTL **15 minut**, max **100** wpisów), co chroni przed *rate limitingiem* i zmniejsza opóźnienia kolejnych zapytań.
   * Klient `RestClient` ma skonfigurowane timeouty (`5s` connect / `5s` read), a błędy sieciowe lub `5xx` przekłada na `ExternalEnergyDataUnavailableException` (HTTP `503`).

6. **Zarządzanie czasem (UTC) i kontekst użytkownika (`X-User-Id`)**
   * Wszystkie daty w bazie i API są przetwarzane w UTC (`Instant`).
   * Identyfikacja użytkownika odbywa się poprzez obowiązkowy nagłówek `X-User-Id` obsługiwany w jednym miejscu (`UserContextResolver`), co ułatwia późniejsze zastąpienie przez Spring Security. Brak lub pusty nagłówek skutkuje `400 Bad Request`.

7. **Ręczne mappery zamiast MapStruct**
   * Mappery (`PersistenceMapper`, `WebResponseMapper`) napisano ręcznie, aby zachować pełną kontrolę nad niezmiennikami Value Objects (`TimeRange`, `Booking.reconstitute`) oraz uniknąć narzutu procesorów adnotacji na Java 25.

---

## Uruchomienie lokalne

### Wymagania
* **Java 25**
* Baza PostgreSQL (np. Supabase — **Session pooler**, port `5432`)

### 1. Przygotowanie schematu w Supabase
Przed pierwszym uruchomieniem utwórz dedykowany schemat w bazie PostgreSQL (np. w Supabase SQL Editor):

```sql
CREATE SCHEMA IF NOT EXISTS ev_charging;
```

### 2. Zmienne środowiskowe
Ustaw adres połączenia z bazą danych (Session pooler, port `5432`):

```bash
export SUPABASE_DB_URL="jdbc:postgresql://<HOST>.pooler.supabase.com:5432/postgres?user=<USER>&password=<PASSWORD>&sslmode=require"
# Opcjonalnie (domyślnie: http://localhost:5173,http://localhost:3000)
export CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000"
```

### 3. Start aplikacji i testy
```bash
cd Backend

# Uruchomienie testów (korzystają z wbudowanego H2 w trybie PostgreSQL - nie wymagają połączenia z Supabase)
./gradlew test

# Uruchomienie serwera backendowego (domyślnie port 8080)
./gradlew bootRun
```

Przy pierwszym starcie na pustej bazie [`ChargerDataSeeder`](Backend/src/main/java/com/jakubstaniszewski/evcharging/backend/infrastructure/adapter/out/persistence/seed/ChargerDataSeeder.java) automatycznie utworzy **4 przykładowe ładowarki** (`Hub-Alpha 150kW`, `Hub-Beta 22kW`, `Hub-Gamma 350kW`, `Hub-Delta 50kW`).

---

## Dokumentacja REST API

Wszystkie błędy zwracane są w standardzie **RFC 9457 (`ProblemDetail`)**:
* `400 Bad Request` — naruszenie walidacji (`@Valid`, niepoprawny zakres dat/godzin, brak nagłówka `X-User-Id`)
* `403 Forbidden` — próba anulowania cudzej rezerwacji
* `404 Not Found` — brak ładowarki lub rezerwacji
* `409 Conflict` — nakładający się termin rezerwacji na wybranej ładowarce
* `503 Service Unavailable` — niedostępność zewnętrznego API Carbon Intensity lub brak ciągłych danych prognozy

### 1. Miks energetyczny Wielkiej Brytanii
`GET /api/energy-mix?days=3` (`days` opcjonalnie `1–3`, domyślnie `3`)

```json
{
  "days": [
    {
      "date": "2026-12-01",
      "averageFuelShares": {
        "biomass": 5.2,
        "nuclear": 14.8,
        "hydro": 1.6,
        "wind": 38.4,
        "solar": 4.1,
        "gas": 26.5,
        "coal": 0.0,
        "imports": 8.4,
        "other": 1.0
      },
      "cleanEnergyPercentage": 64.1,
      "availableIntervals": 48,
      "expectedIntervals": 48,
      "completenessStatus": "COMPLETE"
    }
  ],
  "cleanestDate": "2026-12-01"
}
```

### 2. Najlepsze okno ładowania
`GET /api/charging-window?hours=3` (`hours` wymagane `1–6`)

```json
{
  "startTime": "2026-12-02T23:00:00Z",
  "endTime": "2026-12-03T02:00:00Z",
  "cleanEnergyPercentage": 78.45,
  "partialData": false
}
```

### 3. Lista ładowarek
`GET /api/chargers`

```json
[
  {
    "id": "11111111-1111-1111-1111-111111111101",
    "name": "Hub-Alpha 150kW",
    "location": "London Paddington Green Hub, Bay 1",
    "connectorType": "CCS2",
    "powerKw": 150
  }
]
```

### 4. Zajęte sloty ładowarki (dla kalendarza na froncie)
`GET /api/chargers/{id}/bookings`

Zwraca zanonimizowane przedziały czasowe (bez `userId`):

```json
[
  {
    "startTime": "2026-12-01T12:00:00Z",
    "endTime": "2026-12-01T14:00:00Z"
  }
]
```

### 5. Utworzenie rezerwacji
`POST /api/bookings`  
Nagłówek: `X-User-Id: driver-1`

```json
// Request
{
  "chargerId": "11111111-1111-1111-1111-111111111101",
  "startTime": "2026-12-01T14:15:00Z",
  "endTime": "2026-12-01T16:15:00Z"
}
```

```json
// Response: 201 Created
{
  "id": "c7b8a910-4f2d-4f6b-a3e1-92d4f019a211",
  "chargerId": "11111111-1111-1111-1111-111111111101",
  "chargerName": "Hub-Alpha 150kW",
  "chargerLocation": "London Paddington Green Hub, Bay 1",
  "userId": "driver-1",
  "startTime": "2026-12-01T14:15:00Z",
  "endTime": "2026-12-01T16:15:00Z",
  "durationHours": 2,
  "createdAt": "2026-11-26T18:50:00Z"
}
```

### 6. Lista rezerwacji użytkownika
`GET /api/bookings`  
Nagłówek: `X-User-Id: driver-1` → `200 OK` (lista obiektów `BookingResponse`).

### 7. Anulowanie rezerwacji
`DELETE /api/bookings/{id}`  
Nagłówek: `X-User-Id: driver-1` → `204 No Content`.

---

## Zakres testów i uzasadnienie

Zestaw **37 testów automatycznych** koncentruje się na krytycznych regułach biznesowych, warunkach brzegowych oraz kontraktach integracyjnych (zamiast testowania trywialnych getterów/setterów):

1. **Testy jednostkowe domeny (bez Springa, milisekundowe wykonanie):**
   * `TimeRangeTest` — styk przedziałów na granicy (`14:00` i `14:00` nie kolidują), częściowe nakładanie, pełne zawieranie, walidacja pełnych godzin przy dowolnej minucie startu.
   * `BookingTest` — fabryka domenowa i specyfikacje (przeszłość, czas trwania poza `1–6h`, kolizja, weryfikacja własności przy anulowaniu).
   * `AvailableIntervalsMeanStrategyTest` — suma czystych źródeł, granica progu kompletności `90%` (`44/48` vs `43/48`), przypadek `0` slotów (`UNAVAILABLE`).
   * `ChargingWindowOptimizerTest` — przejście przez północ UTC, remis (wybór wcześniejszego okna), obsługa luk czasowych pomiędzy interwałami.
2. **Testy warstwy aplikacji (Mockito + stały `Clock`):**
   * `BookingApplicationServiceTest` i `EnergyMixServiceTest` — weryfikacja orkiestracji (blokada pesymistyczna, brak zapisu przy konflikcie/walidacji, filtrowanie granicy dób UTC `23:30–00:00`).
3. **Testy integracyjne i warstwowe:**
   * `CarbonIntensityApiClientTest` (`@RestClientTest` + `MockRestServiceServer`) — deserializacja formatu `yyyy-MM-dd'T'HH:mm'Z'`, pusta odpowiedź, błąd `5xx` oraz timeout (`SocketTimeoutException` → `ExternalEnergyDataUnavailableException`).
   * `BookingControllerWebMvcTest` (`@WebMvcTest`) — walidacja `@Valid`, brak `X-User-Id` (`400`), konflikt (`409`), brak uprawnień (`403`), niedostępność API (`503`) i format `ProblemDetail`.
   * `BookingPersistenceAdapterTest` (`@DataJpaTest` na H2 w trybie `PostgreSQL`) — weryfikacja zapytania JPQL o nakładanie przedziałów, niezależność różnych ładowarek i sortowanie wyników.
