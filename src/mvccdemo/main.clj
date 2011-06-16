(ns mvccdemo.main
  (:gen-class))

;; 
;; This is a small demo that shows MVCC/STM in work. 
;;
;; It is more explicity than DRY on purpose.
;;
;;
;; Here is a possible output:
;;
;;    --> increment has been invoked
;;    --> increment reads the current value
;;    --> decrement has been invoked
;;    --> decrement reads the current value
;;    --> decrementing now
;;    --> decrementing done
;;    --> incrementing now
;;    --> increment reads the current value
;;    --> incrementing now
;;    --> incrementing done
;;    final counter: 0
;;    last operation: incremented
;;
;;
;; Note that 
;;    --> increment reads the current value
;;    --> incrementing now
;; appears twice, this due to the transaction.
;; 

(def rnd (new java.util.Random))  

(def counter (ref 0))
(def last-counter-action (ref "none"))

(defn increment []
  "increments the counter and sets last-counter-action to 'incremented'"
  (println "--> increment has been invoked")
  (dosync
    (do
      (println "--> increment reads the current value")
      (def my_counter_value (deref counter))
      (Thread/sleep (. rnd nextInt 1000))
      (println "--> incrementing now")
      (ref-set counter (+ my_counter_value 1)) 
      (ref-set last-counter-action "incremented")
      (println "--> incrementing done")))) 

(defn decrement []
  "decrements the counter and sets last-counter-action to 'incremented'"
  (println "--> decrement has been invoked")
  (dosync
    (do
      (println "--> decrement reads the current value")
      (def my_counter_value (deref counter))
      (Thread/sleep (. rnd nextInt 1000))
      (println "--> decrementing now")
      (ref-set counter (- my_counter_value 1)) 
      (ref-set last-counter-action "decremented")
      (println "--> decrementing done"))))

(defn report []
  (println (str "final counter: " (deref counter))) 
  (println (str "last action: "(deref last-counter-action))))

(defn -main [& args]
  (.start (Thread. (fn [] (increment)))) 
  (.start (Thread. (fn [] (decrement))))
  (Thread/sleep 2000)
  (report))
