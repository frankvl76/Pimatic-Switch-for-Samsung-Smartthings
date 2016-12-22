# Pimatic-Switch-for-Samsung-Smartthings
Connecting Pimatic switches to a Samsung SmartHub

I wanted to buy IoT switches for my ST Hub but found out that there's an alternative which is WAY WAY cheaper. Buy RF-433 switches, but a Raspberry PI, buy a (cheap) transmitter and hook that up to the GPIO.

Install and configure Pimatic : https://pimatic.org/guide/getting-started/installation/

If you want to connect your cheap RF-433 switches to Pimatic you will need these pimatic plugins :
1. HomeDuino https://github.com/pimatic/pimatic-homeduino
2. Gpio https://github.com/pimatic/pimatic-gpio

In the ST IDE, copy paste the contents of the groovy file in a Device Handler (from code) and you will be good to go. 
