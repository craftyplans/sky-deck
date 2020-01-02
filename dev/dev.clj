(ns dev
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [puget.printer]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [orchestra.spec.test :as st]
            [sky-deck.mutations :as sd.mutations]
            [honeysql.core :as sql]))

(set! s/*explain-out* expound/printer)
(st/instrument)
(s/check-asserts true)

(defn load-tap [] (add-tap (bound-fn* puget.printer/cprint)))

(defonce taps (load-tap))

(comment

  (sql/format
    (sd.mutations/generate-person {:new-id        (sd.mutations/new-id)
                                   :person-inputs {:email    "iwillig@gmail.com"
                                                   :username "ivan"
                                                   :password "password"}}))

  (sd.mutations/generate-campaign {:campaign-inputs {}})

  )
