# Icons

### Adding Icons

The actual image files go in `/res/drawable-nodpi`.

You must add them to `/res/xml/appfilter.xml` and `/res/xml/drawable.xml`, along with `/assets/appfilter.xml` and `/assets/drawable.xml`.

### Name Display

The dashboard extracts the actual name of apps from their file name. Here are examples of how they can be formatted...

*All letters/numbers - first letter is capitalized:*

**betternet.png** -> **Betternet**

*A single underscore is a space, the first letter after it is capitalized:*

**google_chrome.png** -> **Google Chrome**

*Double underscores skip a space but make the next letter capital:*

**pay__pal.png** -> **PayPal**

*An underscore at the beginning of a filename makes the first letter lowercase:*

**_kaip** -> **kaip**

*A triple underscore at the beginning makes the next word all capital:*

**___kaip.png** -> **KAIP**

*Another triple underscore example:*

**___npr_one.png** -> **NPR One**

*One more triple underscore example:*

**material___os.png** -> **MaterialOS**

---

# Wallpapers

### URL

To change the URL used to download wallpapers, navigate to `/res/values/dev_options.xml`. There's a string called `wallpapers_json_url`.

```xml
<string name="wallpapers_json_url" formatted="false">
	https://raw.githubusercontent.com/TWellington/JSON-for-Gaufrer/master/wallpapers.json
</string>
```

### File Format

The file the URL points to needs to be a JSON file, in a format like this:

```json
{
	"wallpapers": [
		{
			"author": "Tom Wellington",
			"url": "https://raw.githubusercontent.com/TWellington/JSON-for-Gaufrer/master/Cardboard%20Clouds.png",
			"name": "Cardboard Clouds"
		},
		{
			"author": "Tom Wellington",
			"url": "https://github.com/TWellington/JSON-for-Gaufrer/raw/master/Flakey.png",
			"name": "Flakey"
		}
	]
}
```

The file contains an array called `wallpapers`, which is a list of objects containing an *author*, *url*, and *name*. If the file format doesn't match that exactly, the dashboard will not be able to read your wallpapers.