(ns sky-deck.test-http-auth
  (:require [clojure.test :as t]
            [integrant.core :as ig]
            [clojure.spec.alpha :as s]
            [jsonista.core :as j]
            [expound.alpha :as expound]
            [orchestra.spec.test :as st]
            [juxt.iota :as i]
            [clojure.spec.gen.alpha :as gen]
            [ring.mock.request :as mock]
            [yada.yada :as yada]
            [sky-deck.config :as sd.config]))

(set! s/*explain-out* expound/printer)
(st/instrument)
(s/check-asserts true)

(comment
  (yada/path-for (:sky-engine/routes system) :sky-deck.resource/login))

(defn init-test-system!
  []
  (let [config (sd.config/new-system :test)
        system (ig/init (dissoc config :sky-deck/http-server))]
    system))

(defn with-system-fn
  [func]
  (let [system (init-test-system!)]
    (try (let [] (func))
         (finally (ig/halt! system)))))

(defmacro with-system
  [system-symbol & body]
  `(let [~system-symbol (init-test-system!)]
     (try
       (do ~@body)
       (finally (ig/halt! ~system-symbol)))))

(defn- request
  [{::keys [system method route-uri params]}]
  (let [handler (yada.handler/as-handler (:sky-deck/routes system))
        request (-> (mock/request method route-uri)
                    (mock/json-body params))
        response @(handler request)]
    {:request request
     :response response}))

(t/deftest test-create-person
  (with-system
    system

    (let [session (request {::system system ::method :get ::route-uri "/login"})]
      (t/is (false? true))
      (t/is (= #{} (keys system)))
      (t/is (= {} {})))))

(t/deftest test-create-campaign
  (let [])
  (t/is (false? true)))

(t/deftest test-basic-auth
  (t/is (false? true)))

(t/deftest test-anonymous-character-create
  (t/is (false? true)))
