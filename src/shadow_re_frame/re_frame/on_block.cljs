(ns shadow-re-frame.re-frame.on-block
  (:require [re-frame.core :as rf]
            [shadow-re-frame.interop.contracts :as inter.con]
            [shadow-re-frame.interop.contstants :as const]
            [shadow-re-frame.interop.ethers :as ethers]
            [goog.functions :refer [debounce]]))


(defonce heartbeat
  (.on ethers/provider "block" (debounce. #(rf/dispatch [::new-block %])
                                          2000)))

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
                         (:weth-zapper const/contract->address)]]])})))
