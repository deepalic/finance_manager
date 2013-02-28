(ns finance_manager.server
  (:require [noir.server :as server]
            [finance_manager.models.db :as db]))

(server/load-views-ns 'finance_manager.views)

(defn init
  []
 ; (db/create-user-table)
  ;(db/create-budget-table)
  ;(db/create-expenses-table)
 ; (db/alter-table-budget)
   (db/create-reminder-table)
)

;(defn -main []
  ;(init))

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'finance_manager})))

