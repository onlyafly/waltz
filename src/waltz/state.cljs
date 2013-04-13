(ns waltz.state
  (:refer-clojure :exclude [set]))

(declare get-name)

;; ## Internal Functions

(defn debug-log [sm v & vs]
  (when (and js/console
             (@sm :debug))
    (let [s (apply str (get-name sm) " :: " v vs)]
      (.log js/console s))))

(defn ->coll
  "Wrap a non-collection in a vector."
  [v]
  (if (coll? v)
    v
    [v]))

(defn assoc-sm [sm ks v]
  (swap! sm #(assoc-in % ks v)))

(defn update-sm [sm & fntail]
  (swap! sm #(apply update-in % fntail)))

;; ## State Machine Creation

(defn machine
  "Initialize a state machine object."
  [& [n]]
  (atom {:debug true
         :name (name n)
         :current #{}
         :states {}
         :events {}}))

;; ## Macro Support Functions

(defn add-state
  "Internal function. Use waltz.macros/defstate instead."
  [sm name v]
  (assoc-sm sm [:states name] v))

(defn add-event
  "Internal function. Use waltz.macros/defevent instead."
  [sm name v]
  (assoc-sm sm [:events name] v))

(defn in*
  "Internal function. Use waltz.macros/in instead."
  [state fn]
  (update-in state [:in] conj fn))

(defn out*
  "Internal function. Use waltz.macros/out instead."
  [state fn]
  (update-in state [:out] conj fn))

(defn state*
  "Internal function."
  []
  {:in []
   :out []
   :constraints []})

;; ## Operations on State Machines

(defn get-in-sm [sm ks]
  (get-in @sm ks))

(defn get-name [sm]
  (get-in-sm sm [:name]))

(defn current [sm]
  (get-in-sm sm [:current]))

(defn in?
  "Is the SM currently in the given state?"
  [sm state]
  ((current sm) state))

(defn has-state?
  "Does the SM have the given state?"
  [sm state]
  (get-in-sm sm [:states state]))

(defn has-event?
  "Does the SM have the given event?"
  [sm event]
  (get-in-sm sm [:events event]))

(defn constraint [state fn]
  (update-in state [:constraint] conj fn))

(defn can-transition? [sm state]
  (let [trans (get-in-sm sm [:states state :constraints])]
    (if trans
      (every? #(% state) trans)
      true)))

(defn set
  "Set a new state (or a sequence of states). Pass all additional arguments as
   context to the state's in function."
  [sm states & context]
  (doseq [state (->coll states)]
    (when (can-transition? sm state)
      (let [cur-in (get-in-sm sm [:states state :in])]
        (update-sm sm [:current] conj state)
        (debug-log sm "(set " (str state) ") -> " (pr-str (current sm))) 
        (when (seq cur-in)
          (debug-log sm "(in " (str state) ")")
          (doseq [func cur-in]
            (apply func context))))))
  sm)

(defn unset
  "Unset a state (or a sequence of states). Pass all additional arguments as
   context to the state's out function."
  [sm states & context]
  (doseq [state (->coll states)]
    (when (in? sm state)
      (let [cur-out (get-in-sm sm [:states state :out])]
        (update-sm sm [:current] disj state)
        (debug-log sm "(unset " (str state ")") " -> " (pr-str (current sm)))
        (when (seq cur-out)
          (debug-log sm "(out " (str state) ")")
          (doseq [func cur-out]
            (apply func context))))))
  sm)

(defn set-ex
  "Move from one state (or sequence of states) to another state (or sequence of
   states), passing all additional arguments as context to the states."
  [sm to-unset to-set & context]
  (apply unset sm to-unset context)
  (apply set sm to-set context))

(defn trigger
  "Trigger an event (or an array of events). Pass all additional arguments as
   context to the event function."
  [sm events & context]
  (doseq [event (->coll events)]
    (when-let [event-fn (get-in-sm sm [:events event])]
      (let [result (apply event-fn context)]
        (debug-log sm "(trigger " (str event) ") -> " (boolean result) " :: context " (pr-str context))))))

(defn set-debug
  "Turn on or off debug logging."
  [sm dbg]
  (assoc-sm sm [:debug] dbg))

