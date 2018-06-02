# Delay Map
A Java Implementation of a Delayed Map without a 'stampeding herd'

A normal implementation of a delayed map calls `.notify()` on a single `Object`, and as such every caller waiting on `get` is notified, and they all slowly perform checks. (Hence 'stampeding herd')

The implementation is different. Each waiter has a dedicated `Object`, and only that `Object`'s `.notify()` is called
