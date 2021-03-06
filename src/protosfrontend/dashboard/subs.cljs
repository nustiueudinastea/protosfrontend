(ns dashboard.subs
    (:require
        [re-frame.core :as rf]
        [clairvoyant.core :refer-macros [trace-forms]]
        [re-frame-tracer.core :refer [tracer]]))

(trace-forms {:tracer (tracer :color "blue")}

(rf/reg-sub
  :apps
  (fn apps-sub
    [db _]
    (-> db
        :apps)))

(rf/reg-sub
  :installers
  (fn installers-sub
    [db _]
    (-> db
        :installers)))

(rf/reg-sub
  :installer
  (fn installer-sub
    [db [_ installer-id]]
    (get-in db [:installers (keyword installer-id)])))

(rf/reg-sub
  :installer-params
  (fn installer-params-sub
    [db [_ installer-id]]
    (get-in db [:installers (keyword installer-id) :metadata :params])))

(rf/reg-sub
  :resources
  (fn resources-sub
    [db _]
    (-> db
        :resources)))

(rf/reg-sub
  :create-app-form
  (fn create-app-form-sub
    [db _]
    (get-in db [:create-app :form])))

(rf/reg-sub
  :alert-dashboard
  (fn alert-dashboard-sub
    [db _]
    (get-in db [:dashboard :alert])))

)
