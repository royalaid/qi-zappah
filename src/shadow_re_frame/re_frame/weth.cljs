(ns shadow-re-frame.re-frame.weth
  (:require [promesa.core :as p]
            [shadow-re-frame.interop.contracts :as inter.con]
            [shadow-re-frame.interop.ethers :as inter.ethers]
            [re-frame.core :as rf]
            [applied-science.js-interop :as j]))

(rf/reg-sub ::zapper-allowance
  (fn [db _]
    (some-> (get-in db [:contracts :weth-zapper :allowance]))))

(rf/reg-event-db ::successfully-zapped-balance
  (fn [db [_ token-name]]
    ;(js/console.log token-name)
    (->> token-name
         inter.ethers/format-ether
         (assoc-in db [:tokens :weth :zapped-balance]))))

(rf/reg-event-fx ::zap
  (fn [_ [_ contract-address amount]]
    {:promise {:call (fn []
                       (-> contract-address
                           (p/then #(j/call % :camZap (inter.ethers/parse-units amount)))))
               :on-success [::successfully-zapped-balance]
               :on-failure [:foo]}}))

(rf/reg-event-db ::save-name
  (fn [db [_ token-name]]
    (->> token-name
         (assoc-in db [:tokens :weth :name]))))

(rf/reg-event-fx ::fetch-name
  (fn [_ [_ contract]]
    {:promise {:call #(p/then contract inter.con/name)
               :on-success [::save-name]
               :on-failure [:foo]}}))

(rf/reg-sub ::balance
  (fn [db _]
    (get-in db [:tokens :weth :balance])))

(rf/reg-event-db ::save-balance
  (fn [db [_ token-name]]
    ;(js/console.log token-name)
    (->> token-name
         inter.ethers/format-ether
         (assoc-in db [:tokens :weth :balance]))))

(rf/reg-event-fx ::fetch-balance
  (fn [_ [_ contract-address address]]
    {:promise {:call (fn []
                       (-> contract-address
                           (p/chain #(inter.con/balance-of % address))))
               :on-success [::save-balance]
               :on-failure [:foo]}}))

(rf/reg-event-db ::save-zapper-allowance
  (fn [db [_ token-name]]
    ;(js/console.log token-name)
    (->> token-name
         inter.ethers/format-ether
         (assoc-in db [:contracts :weth-zapper :allowance]))))

(rf/reg-event-fx ::fetch-zapper-alllowance
  (fn [_ [_ contract-address address spender]]
    {:promise {:call (fn []
                       (-> contract-address
                           (p/chain #(inter.con/allowance % address spender))))
               :on-success [::save-zapper-allowance]
               :on-failure [:foo]}}))

(rf/reg-event-db ::successfully-approved-balance
  (fn [db [_ token-name]]
    ;(js/console.log token-name)
    (->> token-name
         inter.ethers/format-ether
         (assoc-in db [:tokens :weth :approved-balance]))))

(rf/reg-event-fx ::approve-balance
  (fn [_ [_ contract-address amount]]
    ;(js/console.dir (clj->js {:approved-amount (inter.ethers/parse-units amount)}))
    {:promise {:call (fn []
                       (-> contract-address
                           (p/chain #(inter.con/approve %
                                                        contract-address
                                                        (inter.ethers/parse-units amount)))))
               :on-success [::successfully-approved-balance]
               :on-failure [:foo]}}))

(rf/reg-event-db :foo
  (fn [db [_ e]]
    (js/console.warn e)
    (assoc-in db [:errors] e)))
