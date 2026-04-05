TEST_CMD = ./gradlew test

.PHONY: test test-auth test-ride test-request test-driver test-chat test-notification run build clean db-up db-down db-restart db-logs

# --- Testing ---

test:
	$(TEST_CMD)

test-auth:
	$(TEST_CMD) --tests "com.xyrel.app.controller.auth.*"

test-passenger:
	$(TEST_CMD) --tests "com.xyrel.app.controller.passenger.*"

test-driver:
	$(TEST_CMD) --tests "com.xyrel.app.controller.driver.*"

test-chat:
	$(TEST_CMD) --tests "com.xyrel.app.controller.chat.*"

test-notification:
	$(TEST_CMD) --tests "com.xyrel.app.controller.notification.*"

# --- Common ---

run:
	./gradlew bootRun

build:
	./gradlew build

clean:
	./gradlew clean

docker-up:
	docker-compose up -d

docker-down:
	docker-compose down

# --- Database ---

db-up:
	docker-compose up -d postgres

db-down:
	docker-compose stop postgres

db-restart:
	docker-compose restart postgres

db-logs:
	docker-compose logs -f postgres
