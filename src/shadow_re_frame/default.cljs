(ns shadow-re-frame.default
  (:require
   [applied-science.js-interop :as j]
   [promesa.core :as p]

   [reagent.core :as r]
   [re-frame.core :as rf]
   [tailwind-hiccup.core :refer [tw]]

   [goog.dom :as gdom]
   [reagent.dom :as rdom]

   [reitit.frontend :as reit.f]
   [reitit.frontend.easy :as reit.fe]
   [reitit.coercion.spec :as reit.spec]
   [spec-tools.data-spec :as ds]

   ["ethers" :as ethers]
   [shadow-re-frame.re-frame.ethers :as rf.ethers]
   [shadow-re-frame.views.zapper-form :as v.zapper-form]
   [shadow-re-frame.interop.contracts :as inter.con]
   [shadow-re-frame.re-frame.on-block :as heartbeat]

   [shadow-re-frame.components.buttons :as cmps.btn]
   [shadow-re-frame.components.forms :as cmps.form]

   [fork.re-frame :as fork]
   [fork.re-frame :as fork-re-frame]
   [cuerdas.core :as str]))

(defonce tap-added (add-tap #(-> % clj->js js/console.dir)))

(defn about-page []
  [:div
   [:h2 "About frontend"]
   [:ul
    [:li [:a {:href "http://google.com"} "external link"]]
    [:li [:a {:href (reit.fe/href ::foobar)} "Missing route"]]
    [:li [:a {:href (reit.fe/href ::item)} "Missing route params"]]]

   [:div
    {:content-editable true
     :suppressContentEditableWarning true}
    [:p "Link inside contentEditable element is ignored."]
    [:a {:href (reit.fe/href ::frontpage)} "Link"]]])

(defn item-page [match]
  (let [{:keys [path query]} (:parameters match)
        {:keys [id]} path]
    [:div
     [:img {:src "/assets/QiDaoDay.svg"}]
     [:h2 "Selected item " id]
     (if (:foo query)
       [:p "Optional foo query param: " (:foo query)])]))

(defonce match (r/atom nil))

(defn current-page []

  [:div
   (let [link-classes [:px-3 :py-2 :flex :items-center :text-xs :uppercase :font-bold :leading-snug :text-white :hover:opacity-75]]
     [:nav (tw [:relative :flex :items-center :justify-between :px-2 :py-3 :bg-green-500 :mb-3])
       [:div (tw [:container :px-4 :mx-auto :flex :flex-wrap :items-center :justify-between])
        [:div (tw [:w-full :relative :flex :justify-between :lg:w-auto :px-4 :lg:static :lg:block :lg:justify-start])
         [:a (tw [:text-sm :font-bold :leading-relaxed :inline-block :mr-4 :py-2 :whitespace-nowrap :uppercase :text-white] {:href "#pablo"}) "teal Color"]]
        [:div#example-navbar-warning (tw [:lg:flex :flex-grow :items-center])
         [:ul (tw [:flex :flex-col :lg:flex-row :list-none :ml-auto])
          [:li (tw [:nav-item])
           [:a (tw link-classes {:href (reit.fe/href ::weth-zapper)})
            "WETH"]]
          [:li (tw [:nav-item])
           [:a (tw link-classes {:href (reit.fe/href ::wmatic-zapper)})
            "WMatic"]]
          [:li (tw [:nav-item])
           [:a (tw link-classes {:href (reit.fe/href ::aave-zapper)})
            "Aave"]]]]]
       [:div (tw (concat [:sm:hidden] link-classes))
        (let [acc @(rf/subscribe [::rf.ethers/account])
              f (take 7 acc)
              l (take-last 5 acc)]
          (str/join (concat f ["..."] l)))]])
   (if @match
     (let [view (:view (:data @match))]
       [view @match]))
   [:pre @match]])

(def routes
  [#_["/"
      {:name ::frontpage
       :foo :bar
       :view v.zapper-form/render}]

   ["/weth-zapper"
    {:name ::weth-zapper
     :asset-contract inter.con/weth-contract
     :zapper-contract inter.con/weth-zapper
     :token-key :weth
     :zapper-key :weth-zapper
     :view v.zapper-form/render}]

   ["/wmatic-zapper"
    {:name ::wmatic-zapper
     :asset-contract inter.con/wmatic-contract
     :zapper-contract inter.con/wmatic-zapper
     :token-key :wmatic
     :zapper-key :wmatic-zapper
     :view v.zapper-form/render}]

   ["/aave-zapper"
     {:name ::aave-zapper
      :asset-contract inter.con/aave-contract
      :zapper-contract inter.con/aave-zapper
      :token-key :aave
      :zapper-key :aave-zapper
      :view v.zapper-form/render}]])

(defn ^:export ^:dev/after-load render []
  (reit.fe/start! (reit.f/router routes {:data {:coercion reit.spec/coercion}})
                  (fn [m] (reset! match m))
                  {:use-fragment false})
  (rdom/render [current-page]
               (gdom/getRequiredElement "shadow-re-frame")))

(defn ^:export init []
  (rf/dispatch-sync [:initialize])
  (render))

