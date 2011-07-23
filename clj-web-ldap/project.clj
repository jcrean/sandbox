(defproject clj-web-ldap "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.2.0"]
         	 [org.clojure/clojure-contrib "1.2.0"]
                 [clj-ldap "0.0.4"]
                 [compojure/compojure "0.6.5"]]
  :dev-dependencies [[swank-clojure "1.2.0"]
                     [lein-ring  "0.4.5"]
                     [ring/ring-jetty-adapter "0.3.11"]]
  :ring {:handler clj-web-ldap.core/app})
