(ns shadow-re-frame.interop.ethers
  (:require [promesa.core :as p]
            [applied-science.js-interop :as j]
            ["ethers" :as ethers]
            [re-frame.core :as rf]
            [shadow-re-frame.interop.contstants :as const]))

;(defonce enable-metamask (p/let [res (.enable js/window.ethereum)])
;                  ethereum         res)


(defonce provider (ethers/providers.Web3Provider. js/window.ethereum "any"))

(defonce signer (j/call provider :getSigner))

(defonce enable-metamask
  (p/let [res (j/call-in provider [:provider :request] #js{:method "eth_requestAccounts"})]
    #_(js/console.dir res)))


(rf/reg-sub ::current-block
  (fn [db _]
    (get-in db [::block-number])))


(defn fetch-contract
  ([contract-address]
   (fetch-contract contract-address contract-address))
  ([contract-address abi-address]
   (p/let [abi-res (js/fetch (str "/assets/contracts/" abi-address ".json"))
           abi-json (.json abi-res)
           contract (ethers/Contract. contract-address
                                      abi-json
                                      signer)]

     contract)))

(defn fetch-current-address
  []
  (j/call signer :getAddress))

(defn format-ether
  [n]
  (j/call-in ethers [:utils :formatEther] n))

(defn parse-units
  ([n]
   (j/call-in ethers [:utils :parseUnits] n))
  ([n unit]
   (j/call-in ethers [:utils :parseUnits] n unit)))

(comment

 (p/let [bal (.getBalance provider "0x44435Bf6AB881133a25bDAaba015Aad0b8A1CDd9")

         qi-dao-masterchef (fetch-contract "0x574Fe4E8120C4Da1741b5Fd45584de7A5b521F0F")


         mai-contract (fetch-contract "0xa3fa99a148fa48d14ed51d610c367c61876997f1")
         mai-bal (j/call mai-contract :balanceOf "0x44435Bf6AB881133a25bDAaba015Aad0b8A1CDd9")
         signer (.getSigner provider)
         address (.getAddress signer)]
    #_#_     qi-dao-masterchef (j/call qi-dao-masterchef :pending 2)

  address
   #_(js/console.log qi-dao-masterchef)))
   ;(js/console.log (ethers/utils.formatEther bal))
   ;(js/console.log (ethers/utils.formatEther mai-bal))))
