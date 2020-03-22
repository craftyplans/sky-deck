(ns sky-deck.graphql-ws
  (:require [cambium.core :as log]
            [jsonista.core :as j]
            [com.walmartlabs.lacinia.parser :as l.parser]
            [com.walmartlabs.lacinia.executor :as l.executor]
            [com.walmartlabs.lacinia.resolve :as l.resolve]
            [manifold.stream :as ms]))

;; https://github.com/juxt/edge/blob/392fa203d3dfd6c1a245f22665c6b5c1e751014b/examples/main/src/edge/yada/graphql_ws.clj#L25

(def json-mapper (j/object-mapper {:decode-key-fn keyword}))

(defn subscription-stream
  [schema q variables
   {:keys [:sky-deck/executor]
    :as   config}]
  (assert schema)
  (assert (map? schema))
  (let [pared-query (l.parser/parse-query schema q nil)
        prepared-query (l.parser/prepare-with-query-variables pared-query variables)
        ctx (merge config
                   {com.walmartlabs.lacinia.constants/parsed-query-key
                    prepared-query})
        source-stream (ms/stream 100 nil executor)

        _ (clojure.pprint/pprint [:query (:operation-type prepared-query)])

        close-fn (l.executor/invoke-streamer
                  ctx
                  (fn callback [value]
                    (let [value (l.executor/execute-query
                                 (assoc ctx ::l.executor/resolved-value value))]
                      (l.resolve/on-deliver!
                       value
                       (fn [result] (ms/put! source-stream result))))))]
    (ms/on-closed source-stream close-fn)
    source-stream))

(defn graphql-error
  [id e]
  {:type    "error"
   :id      id
   :payload (str e)})

(defmulti handle-incoming-ws-message (fn [msg _ctx] (:type msg)))

(defmethod handle-incoming-ws-message "connection_init"
  [msg {:keys [sky-deck.manifold/stream]}]
  (log/info {:msg msg} "got-message")
  (ms/put! stream
           (j/write-value-as-string {:type "connection_ack"} json-mapper)))


(defmethod handle-incoming-ws-message "start"
  [msg
   {:keys [sky-deck.manifold/stream
           sky-deck/graphql-schema
           sky-deck.graphql/subscriptions]
    :as   ctx}]
  (log/info {:msg           msg
             :subscriptions @subscriptions}
            "graphql-start")
  (try
    (let [id (some-> msg
                     :id)
          q (some-> msg
                    :payload
                    :query)
          _ (clojure.pprint/pprint [:msg msg])
          variables (some-> msg :payload :variables)
          source (subscription-stream graphql-schema q variables ctx)]
      (swap! subscriptions assoc id source)
      (ms/connect (ms/transform (map #(j/write-value-as-string {})) source)
                  stream))
    (catch Exception e
      (log/error e "graphql-start-error")
      (ms/put! stream (j/write-value-as-string (graphql-error (:id msg) e))))))




(defmethod handle-incoming-ws-message "stop"
  [msg {:keys [sky-deck.manifold/stream]}]
  (ms/put! stream (j/write-value-as-string {:type "default"} json-mapper)))



(defmethod handle-incoming-ws-message :default
  [msg
   {:keys [sky-deck.manifold/stream]
    :as   _ctx}]
  (ms/put! stream (j/write-value-as-string {:type "default"} json-mapper)))
