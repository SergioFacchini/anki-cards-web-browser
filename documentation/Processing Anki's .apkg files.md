# Processing Anki's .apkg files
## What is this?
This document explains how the processing of the apkg archives should work.

## Structure of .apkg files

The Anki .apkg files are just zip archives containing the following files:
* `collection.anki2`: An sqlite database containing cards, notes, notes models, etc...
* Files named `1`,`2`,`3`,`4`...: The images that the cards use 
* `media`: A json file containing mappings between images and it's original names (eg: file `1` is actually called `latex-96c15f8a1af25e7a2eec64f7c6fedafe12363352.png` in the cards).
 
### The structure of `collection.anki2`
This is the actual database that contains all the notes and other useful 
information that we'll use to generate the viewer. There is a
 [very detailed guide about what exactly the database contains](https://github.com/ankidroid/Anki-Android/wiki/Database-Structure).
In this document we'll just describe what we will need to pull off from the database in order to 
generate the viewer.

### Fetching notes
The notes are stored in the `notes` table. The columns that we need from that table are:
* `id`: The unique identifier of the note
* `mid`: The ID of the cards model (described later how to get it)
* `fld`: The fields of the note. This needs to be feed into the cards model to generate the cards.
         It contains all the fields, separated by the `0x1f` (31) character. The order of the fields 
         is exactly the one presented in the note's model.

### Fetching cards models
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
there is a very nice description of what each field of the model does. Here will be described only
the ones we need to create the cards browser.

In the root of the object, there is a `"1471435193999"` key. That key is the id of that model. 
This is mentioned in the `mid` column of the `notes` table.

Inside that object there is an array with key `"flds"`. This array contains the fields that
the cards specify when creating notes. Usually there are just "Front" and "Rear" fields, however,
depending on the model, it's possible to have more different fields. As mentioned before, the notes
store this information in the `notes.fld` column; the fields of the column are in the same order of 
the fields definitions in this array.

The `"tmpls"` object contains templates of the notes and provides information about the HTML 
structure of the cards. Models that define multiple templates generate multiple cards, one for 
each template. This object has two important fields:
* `"qfmt"`: The HTML of the front of the card
* `"afmt"`: The HTML of the rear of the card
* `"ord"`: The position of the template in the `"tmpls"` array. This is used by the `cards` table to
identify the template that generated the card.

These fields can contain `{{placeholders}}` that have to be substituted with the 
values of the fields that the cards store in `notes.fld`. The association between field position 
and field name is the one of the objects in `"flds"`. Note that `"afmt"` can contain a special 
placeholder called `{{FrontSide}}`; this is just a handy way of showing the content of the card 
without having to copy & paste the contents of `"qfmt"` while creating cards models.

The last field of our interest is `"css"`; it contains the CSS code that have to be applied to the 
card.

### Fetching deck information
The information about the decks contained in the archive is stored in the `col.decks` column in
JSON format. Here is a sample JSON:
 
```
{
  "1": {
    "desc": "",
    "name": "Predefinito",
    "extendRev": 50,
    "usn": 0,
    "collapsed": false,
    "newToday": [ 0, 0],
    "timeToday": [ 0, 0],
    "dyn": 0,
    "extendNew": 10,
    "conf": 1,
    "revToday": [ 0, 0],
    "lrnToday": [ 0, 0],
    "id": 1,
    "mod": 1494099120
  },
  "1493040141981": {
    "extendRev": 50,
    "collapsed": false,
    "newToday": [ 262, 0],
    "timeToday": [ 262, 158105],
    "dyn": 0,
    "extendNew": 10,
    "conf": 1,
    "revToday": [ 262, 11],
    "lrnToday": [ 262, 0],
    "id": 1493040141981,
    "mod": 1494073733,
    "name": "Universit\u00e0 - Calcolatori::Assembly",
    "usn": 661,
    "browserCollapsed": true,
    "mid": 1471435194000,
    "desc": ""
  },
  "1492955368330": {
    "extendRev": 50,
    "collapsed": false,
    "newToday": [ 262, 30],
    "timeToday": [ 262, 1384824],
    "dyn": 0,
    "extendNew": 10,
    "conf": 1,
    "revToday": [ 262, 25],
    "lrnToday": [ 262, 14],
    "id": 1492955368330,
    "mod": 1494095504,
    "name": "Universit\u00e0 - Calcolatori",
    "usn": 671,
    "browserCollapsed": true,
    "mid": 1471435194000,
    "desc": ""
  }
}
```
Some fields that should be noted:
* `id`: Unique id of the deck. The id is used in the `cards` table, in the `did` column to specify 
the associations between cards and decks (explained later).
* `name`: The name of the deck. In case of child decks, the name will be in the format "Parent deck name::Child deck name"

### Fetching cards
The information about cards are stored in the `cards` table. The columns that we need from that table are:
                                                             
* `id`: unique identifier of the card
* `nid`: id of the note that generated this card
* `did`: id identifier of the deck where this card is contained
* `ord`: the position of the template of the model; it matches the template's `ord` field.

### The structure of `media` file
`media` is a JSON file that associates the code-names of the images to its' original names. Note 
that any card's field can contain images, but the images are linked with their complete name. 
During the creation of the .apkg file anki compresses the names of the images (actually it just 
renames these with progressive numbers), however the cards still keep referring to the original
name. The purpose of the `media` file it make us capable to translate numeric names in the 
images's original names.

The structure of `media` is pretty straight-forward:
 
```
{
    "2": "paste-61641370632193.jpg", 
    "3": "paste-81522774245377.jpg", 
    "1": "latex-0cc8b5131ccb25b20258394ebcf13773bb8b2d19.png", 
    "4": "paste-13327283519489.jpg"
}
```

Where the keys are the names of the compressed files and the string values are the original
names.
