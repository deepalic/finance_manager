(ns finance_manager.views.welcome
  (:import (java.lang.String))
  (:require [finance_manager.views.common :as common]
            [noir.content.getting-started]
            [finance_manager.models.db :as db]
            [noir.response :as resp]
            [noir.util.crypt :as crypt]
            [noir.session :as session]
            [noir.validation :as vali])
  (:use [noir.core]
        hiccup.core hiccup.form
        hiccup.element
        [hiccup.page :only [include-css html5 include-js]]))
(require '[clojure.java.jdbc :as sql])


(defpage "/welcome/:user" {:keys [user]}
         (common/layout
           [:p "Welcome to my-website" user]))

;;Code For Signup Form Validation

(defn valid? [{:keys [username password email]}]
  (vali/rule (vali/has-value? username)
             [:username "Username is required"])
  (vali/rule (vali/min-length? password 5)
             [:password "password must be at least 5 characters"]) 
  (vali/rule (vali/has-value? email)
             [:email "EmailId is required"])
  (not (vali/errors? :username :password :email)))


;;Code For Add Budget Form Validation

(defn valid-budget? [{:keys [budget_for budget_amt]}]
  (vali/rule (vali/has-value? budget_for)
             [:budget_for "Budget Date Is Required."])
  (vali/rule (vali/has-value? budget_amt)
             [:budget_amt "Budget Amount Is Required."]) 
  (not (vali/errors? :budget_for :budget_amt)))


;;Code For Add Budget Form Validation
(defn valid-expenses? [{:keys [reason exp_amt exp_date]}]
  (vali/rule (vali/has-value? reason)
             [:reason "Expense Is Required."])
  (vali/rule (vali/has-value? exp_amt)
             [:exp_amt "Expense Amount Is Required."]) 
  (vali/rule (vali/has-value? exp_date)
             [:exp_date "Expense Date Is Required."]) 
  (not (vali/errors? :reason :exp_amt :exp_date)))



;;Code For Partial To Handle Validation

(defpartial error-item [[first-error]]
  [:p.error first-error]
)

;; Login Page

(defpage "/" {:keys [error]}
  (common/layout
   [:div.error error]
   [:h2 "Login Page"]
   (form-to {:id "loginform"} [:post "/login"]
     (label "username" "Username")
     (text-field "username")
      [:br]
     (label "password" "Password")
     (password-field "password")
     [:br]
     (submit-button {:class "btn"} "Login")
     (reset-button {:class "btn"} "Cancel")
   )
  )
)

;; Login Page Handler

(defpage [:post "/login"] user
  ;(common/layout
     (let [getuser (db/get-user (:username user))]
            (println "********")
            (println getuser)
            (println (:password getuser))
              ;(if (and getuser (crypt/compare (:password user) (:password getuser)))
              (if (and getuser (crypt/compare (:password user) (:password getuser)))
                (do
                   (session/put! :user (:username user))
                   (println "********")
                   (println (session/get :user))
                   (render "/homepage" (session/get :user))
                )
                 (render "/" (assoc user :error "Login Failed"))
              )
       )
   ;)
)


;;Signup Page


(defpage "/signup" {:keys [error]}
  (common/layout
    [:h3 "Signup Form"]
    [:div.error error]
    (form-to [:post "/signup"]
     (vali/on-error :username error-item)
      (label "username" "Username")
      (text-field "username")
      [:br]
      (vali/on-error :password error-item)
      (label "password" "Password")
      (password-field "password")
      [:br]
      (vali/on-error :email error-item)
      (label "emailid" "Email Id")
      (text-field "email")
      [:br]
      (submit-button "Signup")
      (reset-button "Cancel")
    )
  )
)

;; Signup Page Handler

(defpage [:post "/signup"] user
  (println "In signup function..")
  (if (valid? user)
    (try
      (let [getuser (db/db-read "select * from users where username=?" (:username user))]
        (println getuser)
        (if getuser
          (render "/signup" (assoc user :error "User is already Exists"))
        )
      )
      (db/add-user (update-in user[:password] crypt/encrypt))
      (session/put! :user (:username user))
      (render "/homepage" (session/get! :user))
      (catch Exception ex
        (render "/signup" (assoc user :error (.getMessage ex)))
      )
    )
    (render "/signup")
  )
)

;; Logout handler

(defpage "/logout" []
  (session/clear!)
  (resp/redirect "/"))


;; Home Page

(defpage "/homepage" []
  (common/layout
    [:h2 "Home Page"]
    (html
      ;(link-to "/logout" "Logout")
      [:br]
      (link-to "/addbudget" "Add Monthly Budget")
      [:br]
      (link-to "/viewbudget" "View Budget")
      [:br]
      (link-to "/reminder" "Add Reminder")
      [:br]
      (let [getuser (db/get-user (session/get :user))
            today_dt (common/getcurrentdate)
            getreminder (db/db-read "select *, DATE_FORMAT(reminder_date,'%Y-%m-%d') AS niceDate
                          from reminder where user_id=?" (:id getuser))]
        (for [gtrem getreminder]
          (if (= today_dt (:nicedate gtrem))
          ;(if (.equals today_dt "2013-02-27")
            [:table{:border="1"}
              [:tr
                [:th [:font {:color "red"}] "Your Reminder For"]
              ]
              [:tr
                [:td [:font {:color "red"}] (:reminder_for gtrem)]
              ]
            ]
          )
        )
      )
    )
  )
)

;; Budget Add Page

(defpage "/addbudget" {:keys [error]}
   (common/layout
     [:h2 "Add Monthly Budget"]
     [:div.error error]
     (form-to [:post "/addbudget"]
        (vali/on-error :budget_for error-item)
        (label "budget_for" "Select Budget Date")
        (text-field {:id "budget_datepicker"} "budget_for")
        (hidden-field {:id "budget_month"} "month")
        (hidden-field {:id "budget_year"} "year")
        (hidden-field "total" "0.0")
        [:br]
        (vali/on-error :budget_amt error-item)
        (label "budget_amt" "Budget Amount")
        (text-field "budget_amt")
        [:br]
        (submit-button "Add Budget")
        (reset-button "Cancel")
        [:br]
        (html (link-to "/homepage" "Back"))
     )
   )
)


;; Budget Handler Page

(defpage [:post "/addbudget"] budget ;{:keys [month year budget_amt]}
  (if (valid-budget? budget)
   (let
        [getuser (db/get-user (session/get :user))
         getbudget (first (db/db-read "select * from budget where month=? and year=? and user_id=?" 
                               (:month budget) (:year budget) (:id getuser)))]
         (println getbudget)
         (if (empty? getbudget)
           (try
             (db/add-budget {:month (:month budget) :year (:year budget) :budget_amt (:budget_amt budget) 
                             :total (:total budget) :user_id (:id getuser)})
             (resp/redirect "/homepage")
             (catch Exception ex
               (println "$$$$$$$$$$$$$$$$$$$")
               (println (.getMessage ex))
               (render "/addbudget" (assoc budget :error (.getMessage ex)))
             )
           )
           (render "/addbudget" (assoc budget :error "Budget For This Month Already Added"))
        )
    )
    (render "/addbudget")
  )
)


;; View Budget Page

(defpage "/viewbudget" []
 (common/layout
  (let [getuser (db/get-user (session/get :user))
        budget (db/db-read "select * from budget where user_id =?" (:id getuser))]
    (html
      [:h3 "Budget List Page"]
      [:table{:border="1"}
       [:tr
        [:th "Budget ID"]
        [:th "Month"]
        [:th "Year"]
        [:th "Budget Amount"]
        [:th "Total"]
        [:th]
        [:th]
        [:th]
       ]
       (for [bgt budget]
        [:tr
          [:td (:id bgt)]
          [:td (:month bgt)]
          [:td (:year bgt)]
          [:td (:budget_amt bgt)]
          [:td (:total bgt)]
          (if (> (:total bgt) (:budget_amt bgt))
            [:td.fontcolor "You Budget Exceeds for this Month ...!!"]
          )
          [:td (html (link-to (str "/updatebudget/" (:id bgt)) "Update Budget"))]
          [:td (html (link-to (str "/addexpenses/" (:id bgt)) "Add Expenses"))]
          [:td (html (link-to (str "/viewexpenses/" (:id bgt)) "View Expenses"))]
         ]
       )]
      (link-to "/homepage" "Back")
     )
   )
  )
)

;; Update Budget Page

(defpage "/updatebudget/:id" {:keys [error id]}
   (common/layout
     [:div.error error]
     [:h3 "Update Budget Page"]
     (let [bgt (db/get-budget-from-id id)]
       (println bgt)
       (form-to [:post "/updatebudget"]
        (label "bgt_month" "Month")
        (text-field {:readonly "true"} "month" (:month bgt))
        [:br]
        (label "budgetamt" "Budget Amount")
        (text-field "budget_amt" (:budget_amt bgt))
        (hidden-field "id" id)
        [:br]
        (submit-button "Update Budget")
        (reset-button "Cancel")
        [:br]
        (html (link-to "/homepage" "Back"))
       )
    )
   )
)

(defpage [:post "/updatebudget"] budget
  (try
    (let [bgt (db/get-budget-from-id (:id budget))]
      (sql/with-connection db/db
          (sql/update-values :budget ["id=?" (:id budget)] 
                             {:budget_amt (Double/parseDouble (:budget_amt budget))}))
      (resp/redirect "/viewbudget")
    )
    (catch Exception ex
      (println (.getMessage ex))
      (render "/updatebudget/:id" (assoc budget :error (.getMessage ex)))
    )
  )
)


;;Add Expenses Page

(defpage "/addexpenses/:id" {:keys [error id]}
   (common/layout
    [:h2 "Add Daily Expenses"]
    (let [budget (db/get-budget-from-id id)]
      (form-to  {:id "expenseform"} [:post "/addexpenses"]
        (vali/on-error :reason error-item)
        (label "expense" "Expense")
        (text-field "reason")

        (hidden-field "budget_id" id)
        (hidden-field {:id "bgt-total"} "budget_total" (:total budget))
        (hidden-field {:id "bgt-amt"} "budget_amount" (:budget_amt budget))

        [:br]
        (vali/on-error :exp_amt error-item)
        (label "exp_amt" "Expense Amount")
        (text-field {:id "exp_amt"} "exp_amt")
        [:br]

        (vali/on-error :exp_date error-item)
        (label "exp_date" "Date Of Expense")
        (text-field {:id "exp_datepk"} "exp_date")
        (hidden-field {:id "exp_month"} "month")
        [:br]

        (submit-button "Add Expense")
        (reset-button "Cancel")
        [:br]
        (html (link-to "/homepage" "Back"))
      )
   )
  )
)

;; Expense Hnadler Page

(defpage [:post "/addexpenses"]  expense ;{:keys [reason exp_amt exp_date month budget_id]} ;expense
  (if (valid-expenses? expense)
    (let
        [getuser (db/get-user (session/get :user))
         budget (first (db/db-read "select * from budget where user_id =? and month =?" 
                                   (:id getuser) (:month expense)))
         total (:total budget)
         bgt_total (+ total (Integer/parseInt (:exp_amt expense)))]
        (try
          (db/add-expense {:reason (:reason expense) :exp_amt (:exp_amt expense) 
                           :exp_date (:exp_date expense) :budget_id (:budget_id expense)})
           (sql/with-connection db/db
            (sql/update-values :budget ["id=?" (:id budget)] {:total bgt_total}))
          (resp/redirect "/viewbudget")
          (catch Exception ex
             (render "/addexpense/:id" :error (.getMessage ex))
          )
        )
     )
    (render "/addexpenses/:id")
  )
)

;; View Expenses Page

(defpage "/viewexpenses/:id" {:keys [error id]}
 (common/layout
  (let [budget (db/get-budget-from-id id)
        expense (db/db-read "select * from expenses where budget_id =?" (:id budget))]
    (html
      [:h3 "Expenses Page"]
      [:table{:border="1"}
       [:tr
        [:th "Expense ID"]
        [:th "Expense Reason"]
        [:th "Expense Amount"]
        [:th "Expense Date"]
        [:th]
       ]
       (for [exp expense]
        [:tr
          [:td (:id exp)]
          [:td (:reason exp)]
          [:td (:exp_amt exp)]
          [:td (:exp_date exp)]
          [:td (html (link-to (str "/updateexpense/" (:id exp)) "Update"))]
         ]
       )]
      (link-to "/viewbudget" "Back")
     )
   )
  )
)

(defpage "/updateexpense/:id" {:keys [error id]}
  (common/layout
    (include-js "/js/finance_manager.js")
    [:div.error error]
    [:h3 "Update Expense"]
    (let [expense (db/db-read "select * from expenses where id=?" id)]
      (for [exp expense]
      (form-to [:post "/editexpenses"]
             ; (vali/on-error :reason error-item)
              (label "exp_reason" "Expense reason")
              (text-field "reason" (:reason exp))

              (hidden-field "exp_id" id)
              (hidden-field "exp_bgt_id" (:budget_id exp))

              [:br]
            ;  (vali/on-error :exp_amt error-item)
              (label "exp_amt" "Expense Amount")
              (text-field "exp_amt" (:exp_amt exp))

              [:br]
            ;  (vali/on-error :exp_date error-item)
              (label "exp_date" "Expense Date")
              (text-field {:id "exp_datepk_edit"} "exp_date" (:exp_date exp))

              (hidden-field {:id "exp_month"} "month")
              [:br]
              (submit-button "Update")
              (reset-button "Cancel")
        )
      )
   )
  )
)

(defpage [:post "/editexpenses"] expense
  ;(if (valid-expenses? expense)
    (try
      (println (:exp_bgt_id expense))
        (let [get_bgt (db/get-budget-from-id (:exp_bgt_id expense))
            get_exp (db/get-expense-from-id (:exp_id expense))]
         (println get_bgt)
         (sql/with-connection db/db
          (sql/update-values :expenses ["id=?" (:exp_id expense)] {:reason (:reason expense) 
                                                                   :exp_amt (:exp_amt expense)
                                                                   :exp_date (:exp_date expense)})
          (if (> (Double/parseDouble (:exp_amt expense)) (:exp_amt get_exp))
             (let [exp_diff (- (Double/parseDouble (:exp_amt expense)) (:exp_amt get_exp))]
                  (sql/update-values :budget ["id=?" (:exp_bgt_id expense)] 
                                   {:total (+ (:total get_bgt) exp_diff)})
                  (resp/redirect "/viewbudget")
             )
             (let [expense-diff (- (:exp_amt get_exp) (Double/parseDouble (:exp_amt expense)))]
              (sql/update-values :budget ["id=?" (:exp_bgt_id expense)] 
                              {:total (- (:total get_bgt) expense-diff)})
                 (resp/redirect "/viewbudget")
             )
          ) ;;if
          )
        )
         (catch Exception ex
             (println (.getMessage ex))
             (render "/updateexpense/:id" (assoc expense :error (.getMessage ex)))
         )
     )
    ; (render "/updateexpense/:id")
; )
)

;;Add Reminder Page

(defpage "/reminder" {:keys [error]}
  (common/layout
    [:div.error error]
    [:h2 "Add Reminder"]
    (form-to [:post "/reminder"]
      (label "reminder_for" "Reminder For")
      (text-field "reminder_for")
      [:br]
      (label "reminder_date" "Reminder Date")
      (text-field {:id "reminder_date"} "reminder_date")
      [:br]
      (submit-button "Add Reminder")
      (reset-button "Cancel")
    )
  )
)

;;Reminder Handler Page

(defpage [:post "/reminder"] reminder
  (let
       [getuser (db/get-user (session/get :user))]
           (try
             (db/add-reminder {:reminder_for (:reminder_for reminder) 
                               :reminder_date (:reminder_date reminder)
                               :user_id (:id getuser)})
             (resp/redirect "/homepage")
             (catch Exception ex
               (println (.getMessage ex))
               (render "/reminder" (assoc reminder :error (.getMessage ex)))
             )
           )
    )
)
