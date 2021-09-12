(ns shadow-re-frame.re-frame.on-block
  (:require [re-frame.core :as rf]
            [shadow-re-frame.interop.contracts :as inter.con]
            [shadow-re-frame.interop.contstants :as const]
            [shadow-re-frame.interop.ethers :as ethers]
            [goog.functions :refer [debounce]]))


(defonce heartbeat
  (.on ethers/provider "block" (debounce. #(rf/dispatch [::new-block %])
                                          200)))

(rf/reg-event-fx
 ::new-block
 (fn [{:keys [db]} [_ block-number]]
   (let [address (:shadow-re-frame.re-frame.ethers/account db)]
     {:db (assoc db ::block-number block-number)
      :fx (when address
            [[:dispatch [:shadow-re-frame.re-frame.contracts/fetch-balance
                         :weth
                         inter.con/weth-contract
                         address]]
             [:dispatch [:shadow-re-frame.re-frame.contracts/fetch-zapper-alllowance
                         :weth-zapper
                         inter.con/weth-contract
                         address
                         (:weth-zapper const/contract->address)]]

             [:dispatch [:shadow-re-frame.re-frame.contracts/fetch-balance
                         :wmatic
                         inter.con/wmatic-contract
                         address]]
             [:dispatch [:shadow-re-frame.re-frame.contracts/fetch-zapper-alllowance
                         :wmatic-zapper
                         inter.con/wmatic-contract
                         address
                         (:wmatic-zapper const/contract->address)]]

             [:dispatch [:shadow-re-frame.re-frame.contracts/fetch-balance
                         :aave
                         inter.con/aave-contract
                         address]]
             [:dispatch [:shadow-re-frame.re-frame.contracts/fetch-zapper-alllowance
                         :aave-zapper
                         inter.con/aave-contract
                         address
                         (:aave-zapper const/contract->address)]]])})))
