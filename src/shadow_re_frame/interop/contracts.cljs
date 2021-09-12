(ns shadow-re-frame.interop.contracts
  (:require [applied-science.js-interop :as j]
            [promesa.core :as p]
            [shadow-re-frame.interop.contstants :as const]
            [shadow-re-frame.interop.ethers :as ethers])
  (:refer-clojure :exclude [name symbol]))


(defprotocol IERC20
  (name [this])
  (symbol [this])
  (decimals [this])
  (total-supply [this])
  (balance-of [this address])

  (transfer [this recipient amount])
  (allowance [this owner spender])
  (approve [this spender amount])
  (transfer-from [this sender recipient amount]))

(deftype ERC20 [ethers-contract]
  IERC20
  (name [_]
    (j/call ethers-contract :name))
  (symbol [_]
    (j/call ethers-contract :symbol))
  (decimals [_]
    (j/call ethers-contract :decimals))
  (total-supply [_]
    (j/call ethers-contract :totalSupply))
  (balance-of [_ address]
    (j/call ethers-contract :balanceOf address))
  (allowance [_ owner spender]
    (j/call ethers-contract :allowance owner spender))

  (approve [_ spender amount]
    (j/call ethers-contract :approve spender amount))
  (transfer [_ recipient amount]
    (j/call ethers-contract :transfer recipient amount))
  (transfer-from [_ sender recipient amount]
    (j/call ethers-contract :transferFrom sender recipient amount)))

(defonce weth-contract
  (p/let [contract (ethers/fetch-contract
                    (:weth const/contract->address))]
    (->ERC20 contract)))

(defonce weth-zapper
  (p/let [contract (ethers/fetch-contract
                    (:weth-zapper const/contract->address))]
    contract))

(defonce wmatic-contract
  (p/let [contract (ethers/fetch-contract
                    (:wmatic const/contract->address))]
    (->ERC20 contract)))

(defonce wmatic-zapper
  (p/let [contract (ethers/fetch-contract
                    (:wmatic-zapper const/contract->address))]
    contract))

(defonce aave-contract
  (p/let [contract (ethers/fetch-contract
                    (:aave const/contract->address)
                    (:wmatic const/contract->address))]
                     ;;You would expect this keyword to be :aave but because the contract is a proxy we leverage another ERC20 token
    (->ERC20 contract)))

(defonce aave-zapper
  (p/let [contract (ethers/fetch-contract
                    (:aave-zapper const/contract->address))]
    contract))

(comment
 (js/console.log 1)
 (p/then wmatic-contract tap>)
 (-> (p/let [c aave-contract]
       (tap> (name c))
       (name c #_"0x44435Bf6AB881133a25bDAaba015Aad0b8A1CDd9"))))


