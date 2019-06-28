# Akka Typed Playground Application

This project is only to experiment and play around with the newly introduced Akka Typed API.

## Battleship
Battleship game where 2 players play against each other. Each player places their ships in a NxM board. Once they are ready,
their objective is to sink all opponent's ships.
Each player can shoot at a coordinate at a time.
Once all cells of a ship are hit, the ship is sunk. All neighboring cells of a sunken ship take a hit automatically.
Player must announce if a shot was a hit or a miss.
