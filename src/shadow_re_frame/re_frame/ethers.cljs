(ns shadow-re-frame.re-frame.ethers
  (:require [shadow-re-frame.interop.ethers :as ethers]
            [re-promise.core]
            [re-frame.core :as rf]
            [shadow-re-frame.interop.contracts :as inter.con]
            [shadow-re-frame.interop.ethers :as inter.eth]))

(rf/reg-event-fx ::register-on-change-accounts
  (fn [_]
    {:promise {:call ethers/fetch-current-address
               :on-success [::successful-account-fetch]
               :on-failure [:failed-account-fetch]}
     :fx [[:dispatch [:shadow-re-frame.re-frame.contracts/fetch-name :weth inter.con/weth-contract]]]}))

(rf/reg-fx ::attach-heartbeat!
  (fn []
    (inter.eth/enable-signer! @inter.eth/provider)
    (inter.eth/init-heartbeat! @inter.eth/provider)))

(rf/reg-event-fx ::attach-heartbeat
  (fn [_]
    {::attach-heartbeat! nil}))

(rf/reg-fx ::attach-on-account-change
  (fn []
    (.on js/ethereum "accountsChanged"
         (fn [accs]
           (rf/dispatch [::successful-account-fetch accs])
           (inter.eth/enable-signer! @inter.eth/provider)))))

(rf/reg-event-fx ::register-on-change
  (fn [_]
    {::attach-on-account-change nil}))

(rf/reg-event-fx
 ::successful-account-fetch
  (fn [{:keys [db]} [_ [account-address]]]
    {:db (assoc db ::account account-address)
     :fx (if (some? account-address)
           [[:dispatch [::attach-heartbeat]]]
           [[:dispatch [::register-on-change]]])}))

(rf/reg-event-db :failed-account-fetch
  (fn [db [_ account-address]]
    (assoc db ::account account-address)))

(rf/reg-event-fx ::fetch-account
  (fn [_]
    {:promise {:call ethers/fetch-current-address
               :on-success [::successful-account-fetch]
               :on-failure [:failed-account-fetch]}
     :fx [[:dispatch [:shadow-re-frame.re-frame.contracts/fetch-name :weth inter.con/weth-contract]]]

     :db {}}))

(rf/reg-event-fx ::initialize
  (fn [_]
    {:promise {:call ethers/init-provider!}}))


;; 3. Queries
;;    make a query for every kind of 'read' into the db.
;;
;;    - queries are identified by keyword.
;;    - queries can (optionally) take parameters.
;;    - `db` is passed as 1st arg to function.
;;      vector of [query-id & args] is passed as 2nd arg.

(rf/reg-sub ::account
            (fn [db [_ _]]
              (get-in db [::account])))


;(rf/reg-event-fx
; qi-dao-masterchef (fetch-contract "0x574Fe4E8120C4Da1741b5Fd45584de7A5b521F0F"))

(rf/reg-sub ::counter-ids
            (fn [db _]
              (-> (get db ::counters)
                  (keys))))
