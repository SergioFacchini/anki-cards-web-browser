# Processing Anki's .dpkg files
## What is this?
This document explains how the processing of the dpkg archives should work.

## Structure of .dpkg files

The Anki .dpkg files are just zip archives containing the following files:
* `collection.anki2`: An sqlite database containing cards, notes, notes models, etc...
* Files named `1`,`2`,`3`,`4`...: The images that the cards of the archive use 
* `media`: A json file containing mappings between images and it's original names (eg: file `1` is actually called `latex-96c15f8a1af25e7a2eec64f7c6fedafe12363352.png` in the cards).
 
### The structure of `collection.anki2`
This is the actual database that contains all the notes and other useful 
information that we'll use to generate the viewer. There is a
 [very detailed guide about what exactly the database contains](https://github.com/ankidroid/Anki-Android/wiki/Database-Structure).
In this document we'll just describe what we will need to pull off from the database in order to generate the viewer.

### Fetching notes
The notes are stored in the `notes` table. The columns that we need from that table are:
* `id`: The unique identifier of the card
* `mid`: The ID of the cards model (described later how to get it)
* `fld`: The fields of the note. This needs to be feed into the cards model to generate the cards. It contains all the 
         fields, separated by the `0x1f` (31) character. The order of the fields is exactly the one presented in the 
         note's model.

### Fetching cards model
In order to be able to generate the cards from the notes, we need to fetch the cards model. The model tells us how many 
cards have to be generated from a single note, what's the HTML surrounding the note, how the fields of `notes.fld` have 
to be presented.
   
The model can be found in the `models` column of the `col` table. Note that the `col` table always contains only one 
record. The model is stored in a JSON format; this is an example of what a model's JSON with two models looks like:

```
{
  "1471435193999": {
    "vers": [],
    "name": "Istruzioni Assembly",
    "tags": [],
    "did": 1493040141981,
    "usn": 625,
    "req": [
      [ 0, "any", [ 0, 2]],
      [ 1, "any", [ 1, 2]]
    ],
    "flds": [
      {
        "name": "Istruzione",
        "media": [],
        "sticky": false,
        "rtl": false,
        "ord": 0,
        "font": "Arial",
        "size": 20
      },
      {
        "name": "Descrizione",
        "media": [],
        "sticky": false,
        "rtl": false,
        "ord": 1,
        "font": "Arial",
        "size": 20
      },
      {
        "name": "Architettura",
        "media": [],
        "sticky": true,
        "rtl": false,
        "ord": 2,
        "font": "Arial",
        "size": 20
      }
    ],
    "sortf": 0,
    "tmpls": [
      {
        "name": "Carta 1",
        "qfmt": "<div></div>\n<div>Nell'architettura <strong>{{Architettura}}</strong> che cosa fa l'istruzione?</div>\n<div>\n<pre>{{Istruzione}}</pre>\n</div>",
        "did": null,
        "bafmt": "",
        "afmt": "{{FrontSide}}\n\n<hr id=answer>\n\n{{Descrizione}}",
        "ord": 0,
        "bqfmt": ""
      },
      {
        "name": "Carta 2",
        "qfmt": "<div>Quale istruzione <strong>{{Architettura}}</strong> corrisponde alla descrizione?</div>\n<br>\n<div><em>{{Descrizione}}</em></div>",
        "did": null,
        "bafmt": "",
        "afmt": "{{FrontSide}}\n\n<hr id=answer>\n\n<pre>{{Istruzione}}</pre>",
        "ord": 1,
        "bqfmt": ""
      }
    ],
    "mod": 1493150692,
    "latexPost": "\\end{document}",
    "type": 0,
    "id": "1471435193999",
    "css": ".card {\n font-family: arial;\n font-size: 20px;\n text-align: center;\n color: black;\n background-color: white;\n}\n",
    "latexPre": "\\documentclass[12pt]{article}\n\\special{papersize=3in,5in}\n\\usepackage[utf8]{inputenc}\n\\usepackage{amssymb,amsmath}\n\\pagestyle{empty}\n\\setlength{\\parindent}{0in}\n\\begin{document}\n"
  },
  "1471435194000": {
    "vers": [],
    "name": "Base (default)",
    "tags": [],
    "did": 1492955368330,
    "usn": 668,
    "req": [ [ 0, "all", [ 0 ] ] ],
    "flds": [
      {
        "name": "Fronte",
        "media": [],
        "sticky": false,
        "rtl": false,
        "ord": 0,
        "font": "Arial",
        "size": 20
      },
      {
        "name": "Retro",
        "media": [],
        "sticky": false,
        "rtl": false,
        "ord": 1,
        "font": "Arial",
        "size": 20
      }
    ],
    "sortf": 0,
    "tmpls": [
      {
        "name": "Carta 1",
        "qfmt": "{{Fronte}}",
        "did": null,
        "bafmt": "",
        "afmt": "{{FrontSide}}\n<hr id=answer>\n{{Retro}}",
        "ord": 0,
        "bqfmt": ""
      }
    ],
    "mod": 1494091839,
    "latexPost": "\\end{document}",
    "type": 0,
    "id": 1471435194000,
    "css": ".card {\n font-family: Arial;\n font-size: 20px;\n text-align: center;\n color: black;\n background-color: white;\n}\n\nimg {\n  vertical-align: middle;\n  padding-bottom: 7px;\n  padding-right: 6px;\n  padding-left: 6px\n}",
    "latexPre": "\\documentclass[12pt]{article}\n\\special{papersize=3in,5in}\n\\usepackage[utf8]{inputenc}\n\\usepackage{amssymb,amsmath}\n\\pagestyle{empty}\n\\setlength{\\parindent}{0in}\n\\begin{document}\n"
  }
}
```

In the [same URL mentioned before](https://github.com/ankidroid/Anki-Android/wiki/Database-Structure#models-jsonobjects)
there is a very nice description of what each field of the model does. Here will be described only the ones we need to 
create the cards browser.

