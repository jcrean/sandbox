(ns clj-web-ldap.core
  (:require
   [clj-ldap.client     :as ldap]))


(def ldap-server (ldap/connect {:host "ec2-50-19-176-178.compute-1.amazonaws.com"
;;                                :ssl? true
                                :bind-dn "uid=jcrean,ou=users,dc=relayzone,dc=com"
                                :password "jcjcjc"}))




(ldap/bind ldap-server "uid=jcrean,ou=users,dc=relayzone,dc=com" "jcjcjc")

(ldap/get ldap-server "uid=jcrean,ou=users,dc=relayzone,dc=com")