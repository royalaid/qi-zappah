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

(comment
 (js/console.log 1)
 (p/let [contract (shadow-re-frame.interop.ethers/fetch-contract
                   (:aave contract->address)
                   (:wmatic contract->address))
         weth-erc20 (ERC20. contract)
         weth-name (name weth-erc20)
         weth-sym (symbol weth-erc20)]

   (js/console.log weth-name)
   (js/console.log weth-sym)))
