(ns dev
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [puget.printer]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [orchestra.spec.test :as st]
            [honeysql.core :as sql]))

(set! s/*explain-out* expound/printer)
(st/instrument)
(s/check-asserts true)

(defn load-tap [] (add-tap (bound-fn* puget.printer/cprint)))

(defonce taps (load-tap))
