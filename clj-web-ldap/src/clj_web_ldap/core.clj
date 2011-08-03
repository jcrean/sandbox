(ns clj-web-ldap.core
  (:require
   [compojure.route     :as route]
   [compojure.handler   :as handler]
   [dapper.core         :as ldap])
  (:use
   [compojure.core]
   [ring.adapter.jetty  :only [run-jetty]]
   [ring.middleware params file file-info]
   [ring.util [response :only [redirect]]]
   [sandbar auth stateful-session form-authentication validation]
   [hiccup core page-helpers]
   [dapper.core         :only [with-ldap]]))



(defonce *config*
  (atom {:jetty {:port 8080 :join? false}
         :ldap  {:host "ec2-50-19-176-178.compute-1.amazonaws.com"
                 :user-dn-suffix "ou=users,dc=relayzone,dc=com"}}))


(defonce *ldap* (atom nil))

(defn ldap-register []
  (ldap/reregister-ldap!
   :dapper {:host           "ec2-50-19-174-69.compute-1.amazonaws.com"
            :user-id-attr   "uid"
            :user-dn-suffix "ou=users,dc=relayzone,dc=com"
            :pooled?        true
            :pool-size      3}))


(defn authenticate-user [uid pass]
  (with-ldap :dapper
    (ldap/bind (ldap/user-dn uid) pass)))


(def properties
     {:username "Username (admin or member)"
      :password "Password (same as above)"
      :username-validation-error "Enter valid ldap uid"
      :password-validation-error "Enter a password!"})


(defrecord LDAPAdapter []
  FormAuthAdapter
  (load-user
   [this username password]
   (let [login {:username username :password password}]
     (if (authenticate-user username password)
       (merge login {:ldap-bind-success true :roles #{:admin}})
       login)))
  (validate-password
   [this]
   (fn [m]
     (if (:ldap-bind-success m)
       m
       (add-validation-error m "Incorrect username or password!")))))

(defn form-authentication-adapter []
  (merge (LDAPAdapter.) properties))


(defn layout [content]
  (html
   (doctype :html4)
   [:html
    [:body
     [:h2 "hi there"]
     content
     [:div
      (if-let [user (current-username)]
        [:div
         (str "You are logged in as: " user) [:br]
         (link-to "logout" "Logout")]
        [:div
         (link-to "admin" "Login")
         " to access administrative area."])]]]))


(defroutes main-routes
  (GET "/" [] (layout "<h1>Hello World Wide Web!</h1>"))
  (GET "/admin" [] (ensure-authenticated
                     (layout "<h1>You have reached the administrative area!</h1>")))
  (form-authentication-routes (fn [r c]
                                (layout c))
                              (form-authentication-adapter))
  (ANY "*" [] (layout "Something else!")))

(def app
     (-> main-routes
         (with-security form-authentication)
         wrap-stateful-session
         wrap-params
         wrap-file-info))

(defonce *server* (atom nil))

(defn start-server []
  (when (nil? @*server*)
    (reset! *server* (run-jetty (var app) (:jetty @*config*)))))

(defn stop-server []
  (when-not (nil? @*server*)
    (.stop @*server*)
    (reset! *server* nil)))

(defn restart-server []
  (stop-server)
  (start-server))

(defn init []
  (restart-server)
  (ldap-register))


(comment

  (init)

  (ldap/with-ldap :dapper
    (authenticate-user "jcrean" "jcjcjc"))

  (ldap/get @*ldap* "uid=jcrean,ou=users,dc=relayzone,dc=com"))