(ns shadow-re-frame.views.zapper-form
  (:require [re-frame.core :as rf]
            [fork.re-frame :as fork]
            [tailwind-hiccup.core :refer [tw]]

            [shadow-re-frame.interop.ethers :as inter.ethers]
            [shadow-re-frame.re-frame.ethers :as rf.ethers]
            [shadow-re-frame.re-frame.contracts :as rf.contracts]


            [shadow-re-frame.components.forms :as cmps.form]
            [shadow-re-frame.interop.contracts :as inter.con]
            [shadow-re-frame.interop.contstants :as const]))

(rf/reg-event-fx
 ::submit-handler
  (fn [{db :db} [_ {:keys [values dirty path
                           need-token-approval?
                           token-key
                           asset-contract
                           zapper-contract
                           zapper-key]}]]

    ;; dirty tells you whether the values have been touched before submitting.
    ;; Its possible values are nil or a map of changed values
    (let [approval-amount (values "token-approval-val")]
      {#_#_:db (fork/set-submitting db path true)
       :dispatch (if need-token-approval?
                   [::rf.contracts/approve-balance
                    token-key
                    asset-contract
                    (get const/contract->address zapper-key)
                    approval-amount]
                   [::rf.contracts/zap
                    token-key
                    zapper-contract
                    approval-amount])})))

(rf/reg-event-fx
 ::resolved-form
  (fn [{db :db} [_ path values]]
    {:db (fork/set-submitting db path false)}))

(defn my-form
  [{:keys [values handle-change
           handle-blur form-id
           handle-submit props]}]
  (let [{:keys [submit-text]} props]
    [:div
     [:form {:id form-id
             :on-submit handle-submit}
      [cmps.form/input-with-label
       {:label-val "Value To Approve"}
       {:value (values "token-approval-val")
        :id "token-approval-val"
        :name "token-approval-val"
        :type "text"
        :required true
        :on-change handle-change
        :on-blur handle-blur}]
      [:button (tw [:h-12 :px-6 :m-2 :text-lg :text-indigo-100 :transition-colors :duration-150
                    :bg-indigo-700 :rounded-lg :focus:shadow-outline :hover:bg-indigo-800]
                   {:type "submit"
                    #_#_:disabled true})
       submit-text]]]))

(defn render
  [{:keys [data]}]
  (let [{:keys [token-key zapper-key asset-contract zapper-contract]} data]
   [:div (tw [:max-w-lg :mx-auto])
    [:h2 (str "Zapper for " (name token-key))]
    (let [address @(rf/subscribe [::rf.ethers/account])
          block-number @(rf/subscribe [::inter.ethers/current-block])
          weth-balance @(rf/subscribe [::rf.contracts/balance token-key])
          weth-allowance @(rf/subscribe [::rf.contracts/zapper-allowance zapper-key])

          need-token-approval? (<= weth-allowance 0)]
      [:div
       [:div (tw [:flex :p-1])
        [:div (tw [:mr-auto]) "Balance:"]
        [:div (tw [:ml-auto]) weth-balance]]
       [:div (tw [:flex :p-1])
        [:div (tw [:mr-auto]) "Contract Allowance:"]
        [:div (tw [:ml-auto]) weth-allowance]]
       [fork/form {:initial-values {"token-approval-val" 0.0}
                   :props {:submit-text (if need-token-approval?
                                          "Approve" "Zap")}
                   :path [:form]
                   :form-id "form-id"
                   :prevent-default? true
                   :on-submit
                   #(rf/dispatch [::submit-handler (merge %
                                                          {:token-key token-key
                                                           :asset-contract asset-contract
                                                           :zapper-key zapper-key
                                                           :zapper-contract zapper-contract
                                                           :need-token-approval? need-token-approval?})])
                   :clean-on-unmount? true}
        my-form]])]))
