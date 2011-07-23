(ns clj-web-ldap.core
  (:require
   [compojure.route     :as route]
   [compojure.handler   :as handler]
   [clj-ldap.client     :as ldap])
  (:use
   [compojure.core]
   [ring.adapter.jetty  :only [run-jetty]]))



(def ldap-server (ldap/connect {:host "ec2-50-19-176-178.compute-1.amazonaws.com"
;;                                :ssl? true
                                :bind-dn "uid=jcrean,ou=users,dc=relayzone,dc=com"
                                :password "jcjcjc"}))

(defroutes main-routes
  (GET "/" [] "<h1>Hello World Wide Web!</h1>")
  (route/resources "/")
  (route/not-found "Page not found"))


(defn app []
  (handler/site main-routes))

(defonce *server* (atom nil))

(defn start-server []
  (when (nil? @*server*)
    (reset! *server* (run-jetty (app) {:port 8080 :join? false}))))

(defn stop-server []
  (when-not (nil? @*server*)
    (.stop @*server*)
    (reset! *server* nil)))




(comment

  (start-server)

  (stop-server)

  (ldap/bind ldap-server "uid=jcrean,ou=users,dc=relayzone,dc=com" "jcjcjc")

  (ldap/get ldap-server "uid=jcrean,ou=users,dc=relayzone,dc=com"))