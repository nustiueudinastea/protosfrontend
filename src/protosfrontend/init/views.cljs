(ns init.views
  (:require
    [re-frame.core :as rf]
    [protosfrontend.util :as util]
    [reagent-forms.core :refer [bind-fields]]
    [clairvoyant.core :refer-macros [trace-forms]]
    [re-frame-tracer.core :refer [tracer]]))

(trace-forms {:tracer (tracer :color "brown")}

(defn alert [alert-sub]
  (let [alert-data @(rf/subscribe alert-sub)]
    (when alert-data
      [:div {:class "form-group"}
        [:div {:class "form-row text-center"}
          [:div {:class "col-12"}
            [:div {:class (str "alert alert-" (:type alert-data)) :role "alert"} (:message alert-data)]]]])))

(defn navigation-buttons [button-text event disabled? loading?]
  [:div {:class "form-group"}
    [:div {:class "form-row text-center"}
      [:div {:class "col-12"}
        [:button {:type "button" :class "btn btn-icons btn-rounded btn-outline-primary mr-2" :on-click #(rf/dispatch [:decrement-init-step])} [:i {:class "mdi mdi-arrow-left"}]]
        [util/submit-button-spinner button-text event "primary" disabled? loading?]
        [:button {:type "button" :class "btn btn-icons btn-rounded btn-outline-primary" :on-click #(rf/dispatch [:increment-init-step])} [:i {:class "mdi mdi-arrow-right"}]]]]])

(defn input-field [properties]
  ^{:key (:id properties)} [:div {:class "form-group"}
    [:div {:class "input-group"}
      [:input properties]
      [:div {:class "input-group-append"}
        [:span {:class "input-group-text"}
          [:i {:class "mdi mdi-check-circle-outline"}]]]]])

(defn single-select-list [properties items]
  [:div {:class "form-group"}
      [:ul.list-group properties
      (for [item items]
        [:div.list-group-item {:key (:id item)} (:name item)])]])

(defn step1 []
  [:div {:class "col-lg-4 mx-auto"}
    [:h2 {:class "text-center mb-4"} "User & domain"]
    [:div {:class "auto-form-wrapper"}
      [bind-fields
        [:div
          [input-field {:field :text :id :init-wizard.step1.form.username :class "form-control" :placeholder "Username"}]
          [input-field {:field :text :id :init-wizard.step1.form.name :class "form-control" :placeholder "Name"}]
          [input-field {:field :password :id :init-wizard.step1.form.password :class "form-control" :placeholder "Password"}]
          [input-field {:field :password :id :init-wizard.step1.form.confirmpassword :class "form-control" :placeholder "Confirm password"}]
          [input-field {:field :text :id :init-wizard.step1.form.domain :class "form-control" :placeholder "Domain"}]]
        (util/form-events [:init-form :step1])]
      (let [loading? @(rf/subscribe [:loading?])]
          [navigation-buttons "Register" [:register-user-domain] loading? loading?])
      [alert [:alert-init :step1]]]])

(defn step2 []
  [:div {:class "col-lg-4 mx-auto"}
    [:h2 {:class "text-center mb-4"} "Domain provider"]
    [:div {:class "auto-form-wrapper"}
      [:h5 {:class "mb-4"} "Select a DNS provider"]
      (let [dns-providers @(rf/subscribe [:providers :step2])]
        (if-not (empty? dns-providers)
          [bind-fields
            (single-select-list {:field :single-select :id :init-wizard.step2.selected-provider} dns-providers)
            (util/form-events [:init-form :step2])]))
      (let [dns-params @(rf/subscribe [:provider-params :step2])]
        (if-not (empty? dns-params)
          [:div
          [:div {:class "border-top my-3"}]
          [bind-fields
            (into [:div ] (for [field dns-params]
                               [input-field {:field :text :id (keyword (str "init-wizard.step2.form." field)) :class "form-control" :placeholder field}]))
            (util/form-events [:init-form :step2])]]))
      (let [loading? @(rf/subscribe [:loading?])
           disabled? (or (not @(rf/subscribe [:selected-provider :step2])) loading?)
           dns-params @(rf/subscribe [:provider-params :step2])]
        (if (empty? dns-params)
          [navigation-buttons "Download" [:download-dns-provider] disabled? loading?]
          [navigation-buttons "Run" [:create-app-during-init :step2] disabled? loading?]))
      [alert [:alert-init :step2]]]])

(defn step3 []
  [:div {:class "col-lg-4 mx-auto"}
    [:h2 {:class "text-center mb-4"} "TLS certificate provider"]
    [:div {:class "auto-form-wrapper"}
      [:h5 {:class "mb-4"} "Select a certificate provider"]
      (let [providers @(rf/subscribe [:providers :step3])]
        (if-not (empty? providers)
          [bind-fields
            (single-select-list {:field :single-select :id :init-wizard.step3.selected-provider} providers)
            (util/form-events [:init-form :step3])]))
      (let [params @(rf/subscribe [:provider-params :step3])]
        (if-not (empty? params)
          [:div
          [:div {:class "border-top my-3"}]
          [bind-fields
            (into [:div ] (for [field params]
                               [input-field {:field :text :id (keyword (str "init-wizard.step3.form." field)) :class "form-control" :placeholder field}]))
            (util/form-events [:init-form :step3])]]))
      (let [loading? @(rf/subscribe [:loading?])
           disabled? (or (not @(rf/subscribe [:selected-provider :step3])) loading?)
           installer-downloaded @(rf/subscribe [:init-installer-downloaded :step3])
           installer-params @(rf/subscribe [:provider-params :step3])]
        (if (not installer-downloaded)
          [navigation-buttons "Download" [:download-cert-provider] disabled? loading?]
          [navigation-buttons "Run" [:create-app-during-init :step3] disabled? loading?]))
      [alert [:alert-init :step3]]]])

(defn step4 []
  [:div {:class "col-lg-4 mx-auto"}
    [:h2 {:class "text-center mb-4"} "Protos DNS and TLS resources"]
    (let [resources @(rf/subscribe [:init-resources])
          loading? @(rf/subscribe [:loading?])
          resources-created (if (= (count resources) 0)
                                false
                                (every? true? (for [[k v] resources]
                                                   (= (:status v) "created"))))]
    [:div {:class "auto-form-wrapper"}
      [:h5 {:class "mb-4"} "Resource status"]

        [:ul {:class "list-arrow"}
          (for [[id rsc] resources]
            [:li {:key id} (str (:type rsc) " has status: " (:status rsc))])]
        (if resources-created
          [navigation-buttons "Finish" [:restart-and-redirect] loading? loading?]
          [navigation-buttons "Create resurces" [:create-init-resources] loading? loading?])
      [alert [:alert-init :step4]]])])

(defn init-wizard []
  [:div {:class "container-scroller"}
    [:div {:class "container-fluid page-body-wrapper full-page-wrapper auth-page"}
      [:div {:class "content-wrapper d-flex align-items-center auth theme-one"}
        [:div {:class "row w-100"}
        (let [init-step  @(rf/subscribe [:init-step])]
            (condp = init-step
            1         [step1]
            2         [step2]
            3         [step3]
            4         [step4]
            ))]]]])

)
