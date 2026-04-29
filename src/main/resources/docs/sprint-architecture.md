# Architecture par sprint

## Blocs metier

- `common`
  - fragments JSP communs
  - CSS/JS partages
  - controle d'acces, dashboard, pieces justificatives, socle technique
- `sprint1`
  - creation de visa / nouveau titre
- `sprint2/sprint2-initial`
  - duplicata
- `sprint2/sprint2-bis`
  - transfert de visa vers un nouveau passeport
- `sprint3`
  - scan apres creation, duplicata ou transfert
- `sprint4`
  - generation de QR code

## Points de compatibilite

- Les routes HTTP existantes sont conservees.
- Les vues back-office historiques pointent maintenant vers les nouveaux dossiers de sprint.
- Le scan accepte desormais un parametre `id` pour controler une demande precise, sans perdre le scan global.
