# Ecommerce Insights Worker

This worker enriches products with external book metadata from Google Books and Open Library.

## Setup

- Node.js 18+
- MySQL access to the ecommerce database

Install dependencies:

```
npm install
```

## Configuration

Set environment variables (optional defaults shown). The worker reads a local `.env` file if present:

- DB_HOST=localhost
- DB_PORT=3306
- DB_NAME=ecommercedb
- DB_USER=root
- DB_PASSWORD=
- BATCH_SIZE=20
- SLEEP_MS=200
- DRY_RUN=false
- CRON_ENABLED=false
- CRON_SCHEDULE=0 2 * * 1
- RUN_ON_START=true
- LOG_FILE=

## Run

```
npm run start
```

## Scheduling

Set `CRON_ENABLED=true` and optionally customize `CRON_SCHEDULE`.
Default schedule runs every Monday at 02:00.

Example:

```
CRON_ENABLED=true
CRON_SCHEDULE=0 3 * * 0
RUN_ON_START=false
```

## Windows Task Scheduler (Always-On)

Use Task Scheduler to start the worker automatically on boot, then let the worker's internal cron handle the run schedule.

1) Copy `.env.example` to `.env` and set your DB credentials and schedule.
2) Open Task Scheduler and create a new task.
3) Set the trigger to "At startup" (or "At log on" if preferred).
4) Action: Start a program.
	- Program/script: `powershell.exe`
	- Add arguments: `-ExecutionPolicy Bypass -File "C:\path\to\Backend\ecommerce-insights-worker\start-worker.ps1"`
	- Start in: `C:\path\to\Backend\ecommerce-insights-worker`
5) Save the task.

Notes:
- Keep `CRON_ENABLED=true` in `.env` so the schedule is honored.
- If you only want a single run at startup, set `CRON_ENABLED=false`.

## Logging

Set `LOG_FILE` to write logs to a file in addition to console output.

## Notes

- The worker targets products in the Books category or with a non-empty ISBN.
- ISBNs are normalized by removing spaces and hyphens and uppercasing.
- Results are stored in the external_product_insights table (one row per product + source).
