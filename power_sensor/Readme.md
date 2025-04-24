# Power Sensor - Hy-switch (Energy Depot VECTIS)
Basic description see [Readme.md](../Readme.md#control-via-power-sensor---kaco-hy-switch-energy-depot-vectis)

## RJ-45 connection
Cable TIA/EIA 568B

| Pin Number | Color        | Function      |
|------------|--------------|---------------|
| 1          | White-Orange | Differential- |
| 2          | Orange       | Differential+ |
| 3          | White-Green  | Differential+ |
| 4          | Blue         | +18 V          |
| 5          | White-Blue   | +18 V          |
| 6          | Green        | Differential- |
| 7          | White-Brown  | GND           |
| 8          | Brown        | GND           |

18 V was measured as 17.93 V.

Between green and white-green: 1 kOhm  
Between orange and white-orange: 1 kOhm  
Each differential pole uses two parallel wires.  

Bitrate/Baudrate = 230.4 kbit/s  
Message every 10 ms. 

## Protocol reverse engineering
WIP

Test without inverter (12 V supply only).  
Long frame length:  1.732ms or 1.722ms  
Short frame length: 1.648ms or 1.644ms or 1.638ms  
Difference: 0.078ms -> 18bit (start + 16 data + stop)  
Difference: 0.074ms -> 17bit (start + 16 data)


