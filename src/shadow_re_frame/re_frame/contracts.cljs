(ns shadow-re-frame.re-frame.contracts
  (:require [promesa.core :as p]
            [shadow-re-frame.interop.contracts :as inter.con]
            [shadow-re-frame.interop.ethers :as inter.ethers]
            [re-frame.core :as rf]
            [applied-science.js-interop :as j]))

(rf/reg-sub ::zapper-allowance
  (fn [db [_ token-key]]
    (some-> (get-in db [:contracts token-key :allowance])
            js/parseFloat)))

(rf/reg-event-db ::successfully-zapped-balance
  (fn [db [_ token-key zapped-balance]]
    (->> (j/get zapped-balance :value)
         inter.ethers/format-ether
         (assoc-in db [:tokens token-key :zapped-balance]))))

(rf/reg-event-fx ::zap
  (fn [_ [_ token-key contract-address amount]]
    {:promise {:call (fn []
                       (-> contract-address
                           (p/then #(j/call % :camZap (inter.ethers/parse-units amount)))))
               :on-success [::successfully-zapped-balance token-key]
               :on-failure [:foo]}}))

(rf/reg-event-db ::save-name
  (fn [db [_ token-key token-name]]
    (->> token-name
         (assoc-in db [:tokens token-key :name]))))

(rf/reg-event-fx ::fetch-name
  (fn [_ [_ token-key contract]]
    {:promise {:call #(p/then contract inter.con/name)
               :on-success [::save-name token-key]
               :on-failure [:foo]}}))

(rf/reg-sub ::balance
  (fn [db [_ token-key]]
    (some-> (get-in db [:tokens token-key :balance])
            js/parseFloat)))

(rf/reg-event-db ::save-balance
  (fn [db [_ token-key bal-to-save]]
    (->> bal-to-save
         inter.ethers/format-ether
         (assoc-in db [:tokens token-key :balance]))))

(rf/reg-event-fx ::fetch-balance
  (fn [_ [_ token-key contract-address address]]
    {:promise {:call (fn []
                       (-> contract-address
                           (p/chain #(inter.con/balance-of % address))))
               :on-success [::save-balance token-key]
               :on-failure [:foo]}}))

(rf/reg-event-db ::save-zapper-allowance
  (fn [db [_ zapper-key allowance-to-save]]
    (->> allowance-to-save
         inter.ethers/format-ether
         (assoc-in db [:contracts zapper-key :allowance]))))

(rf/reg-event-fx ::fetch-zapper-alllowance
  (fn [_ [_ zapper-key contract-address address spender]]
    {:promise {:call (fn []
                       (-> contract-address
                           (p/chain #(inter.con/allowance % address spender))))
               :on-success [::save-zapper-allowance zapper-key]
               :on-failure [:foo]}}))

(rf/reg-event-fx ::successfully-approved-balance
  (fn [{:keys [db]} [_ token-key approved-bal]]
    {:db (->> (j/get approved-bal :value)
              inter.ethers/format-ether
              (assoc-in db [:tokens token-key :approved-balance]))}))

(rf/reg-event-fx ::approve-balance
  (fn [_ [_ token-key asset-contract contract-address amount]]
    {:promise {:call (fn []
                       (-> asset-contract
                           (p/chain #(inter.con/approve %
                                                        contract-address
                                                        (inter.ethers/parse-units amount)))))
               :on-success [::successfully-approved-balance token-key]
               :on-failure [:foo]}}))

(rf/reg-event-db :foo
  (fn [db [_ e]]
    (js/console.warn e)
    (assoc-in db [:errors] e)))
