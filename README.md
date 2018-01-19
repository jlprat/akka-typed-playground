# Akka Typed Playground Application

This project is only to experiment and play around with the newly introduced Akka Typed API.

## Restaurant App
This sample dummy application will simulate a Restaurant with its tables and waiters doing their business as usual.

## Main use cases

### Maître accommodates guests
When guests arrive, the Maître should accommodate them to an empty table. If there isn't any available, it will ask 
the guests to wait until one becomes free.

### Guests eat, pay and leave
Once on a table, guests will order something, and after a while they will pay for their meal and leave. This sets the
table free again.

## Actors involved

### Table
It models a smart table where guests sit. There are different sizes of tables. The table can be occupied or available.
As a smart table, it handles the food and the bills of the guests 

### Guest Group
It models a group of guest of a restaurant. The group has a specific size, and it can only sit on a table that is bigger
than the group size. The group orders, pays and leaves as a whole.

### Maître
Is the entry point to the restaurant, all guests need to get to the restaurant through this actor. 
