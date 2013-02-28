(ns finance_manager.models.db)

(require '[clojure.java.jdbc :as sql])
;(require '[clojure.contrib.sql :as sql])

(def db
  {:subprotocol "mysql"
   :subname "//localhost:3306/finance_manager_db"
   :user "root"
   :password "a"})

;(def my-data (make-datamap db [:budget [has-many :expenses]]
                              ;[:expenses [belongs-to :budget]]))


(defn create-user-table []
  (sql/with-connection
      db
      (sql/create-table
        :users
        [:id "SERIAL"]
        [:username "varchar(100)"]
        [:password "varchar(100)"]
        [:email "varchar(100)"])
    )
)

(defn create-budget-table []
  (sql/with-connection
      db
      (sql/create-table
        :budget
        [:id :serial "PRIMARY KEY"]
        [:month "varchar(100)"]
        [:year "integer"]
        [:budget_amt "double"]
        [:total "double"]
        [:user_id "integer" "references users (id)"])
    )
)

(defn create-expenses-table []
  (sql/with-connection
      db
      (sql/create-table
        :expenses
        [:id "SERIAL"]
        [:reason "varchar(100)"]
        [:exp_amt "double"]
        [:exp_date "date"]
        [:budget_id "integer" "references budget (id)"]
        )
  )
)

(defn create-reminder-table []
  (sql/with-connection
      db
      (sql/create-table
        :reminder
        [:id "SERIAL"]
        [:reminder_for "varchar(100)"]
        [:reminder_date "date"]
        [:user_id "integer" "references users (id)"]
        )
  )
)


(defn add-user [user]
  (sql/with-connection
    db
    (sql/insert-record :users user)
  )
)

(defn add-budget [budget]
  (sql/with-connection
    db
    (sql/insert-record :budget budget)
  )
)

(defn add-expense [expense]
  (sql/with-connection
    db
    (sql/insert-record :expenses expense)
  )
)

(defn add-reminder [reminder]
  (sql/with-connection
    db
    (sql/insert-record :reminder reminder)
  )
)


(defn db-read [query & args]
  (sql/with-connection
    db
    (sql/with-query-results
      res
      (vec (cons query args)) (doall res))))

(defn get-user [username]
  (first
  (db-read "select * from users where username=?" username))
)

(defn get-budget-from-id [id]
  (first
  (db-read "select * from budget where id=?" id))
)

(defn get-expense-from-id [id]
  (first
  (db-read "select * from expenses where id=?" id))
)
