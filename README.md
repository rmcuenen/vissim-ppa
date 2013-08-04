# VISSIM-PPA Koppeling v1.1rc
Dit is de kandidaat voor versie 1.1 van de VISSIM-PPA Koppelingsapplicatie.

## Versie-informatie
In deze versie zijn de volgende bugs opgelost:

*   [Issue 1](https://github.com/rmcuenen/vissim-ppa/issues/1): In het JSON bericht met MTM-data staat dezelfde waarde voor 'snelheid_kmh' als voor 'intensiteit_vth'.  
*   [Issue 2](https://github.com/rmcuenen/vissim-ppa/issues/2): Wanneer de intensiteit (of snelheid) een integer waarde hebben wordt toch een double waarde in het JSON bericht gezet.  

Ook zijn de volgende verbeteringen aangebracht:

*   [Issue 3](https://github.com/rmcuenen/vissim-ppa/issues/3): Gebruik de Jackson JSON-bibliotheek om de JSON-berichten te genereren.  

## Bestanden
*   _vissim-ppa.xsd_ De XML Schema Definitie van de configuratie.  
*   _vissim-ppa-javadoc.jar_ Het Java Archive bestand met de `javadoc` van de applicatie.  
*   _vissim-ppa-installer.jar_ Dit is de installer die alle benodigde bestanden van de applicatie bevat.  

