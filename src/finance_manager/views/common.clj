(ns finance_manager.views.common
  (:import (java.util.Date)
           (java.text.Format)
           (java.text.SimpleDateFormat))
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css html5 include-js]]
        hiccup.element)
  (:require [noir.session :as session]))


(defn getcurrentdate []
  (let [today (new java.util.Date)
        date_format (new java.text.SimpleDateFormat "yyyy-MM-dd")
        today_date (.format date_format today)
        ]
     today_date
  )
)

(defpartial layout [& content]
            (html5
              [:head
               [:title "finance manager"]
               ;(include-css "/css/reset.css")
               (include-css "/css/app.css")
               (include-css "/css/bootstrap.min.css")
               (include-css "/css/jquery-ui-1.10.1.custom.min.css")
               (include-js "/js/jquery-1.9.1.js")
               (include-js "/js/jquery-ui-1.10.1.custom.min.js")
               (include-js "/js/finance_manager.js")
               ]
              [:body
               [:div.container
                 [:div.masthead
                  [:h3.muted "Finanace Manager"]
                 ; (link-to "/signup" "Signup")
                 ]
                 [:div.jumbotron
                  (if (session/get :user)
                      (link-to "/logout" "Logout")
                      (link-to "/signup" "Signup")
                  )
                   (getcurrentdate)
                   content
                 ]
                ]
               ]))
