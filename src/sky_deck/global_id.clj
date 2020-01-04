(ns sky-deck.global-id
  (:require [clojure.string :as str])
  (:import (java.util Base64)
           (java.util UUID)))

(defn- encode-id
  [x]
  (.encodeToString (Base64/getEncoder) (.getBytes x)))

(defn- decode-id
  [x]
  (String. (.decode (Base64/getDecoder) x)))

(defn to-global-id
  [type id]
  (encode-id (str/join ":" [(name type) id])))

(defn from-global-id
  [global-id]
  (let [[type id] (str/split (decode-id global-id) #":")]
    [(keyword type) (UUID/fromString id)]))

(comment
  (sky-deck.global-id/to-global-id :Person #uuid"5072e832-2d86-11ea-b123-3f04c1c50b65"))
