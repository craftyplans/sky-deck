css:
	npx tailwind build styles.css -o resources/public/css/styles.css

fmt:
	lein zprint dev/**.clj
	lein zprint project.clj
	lein zprint resources/**.edn
	lein zprint src/sky_deck/**.clj
	lein zprint test/sky_deck/**.clj
lint:
	clj-kondo --lint src/
	clj-kondo --lint dev/
	clj-kondo --lint test/

build:
	lein uberjar

.PHONY: fmt build
