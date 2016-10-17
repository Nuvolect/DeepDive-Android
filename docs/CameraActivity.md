# Using CameraActivity

The simplest way to use this library is to use `CameraActivity`. This
gives you the same "API" as you get with the Android SDK's
`ACTION_IMAGE_CAPTURE`, making it fairly easy for you to get existing
`ACTION_IMAGE_CAPTURE` working with your own local camera activity.

## Getting the Intent

The simplest way to craft the right `Intent` to use is to create
a `CameraActivity.IntentBuilder`, call whatever configuration methods
that you want on that builder, and have it `build()` you an `Intent`.
That `Intent` can be used with `startActivityForResult()`, just as you
might have used it with an `ACTION_IMAGE_CAPTURE` `Intent`.

Under the covers, `CameraActivity.IntentBuilder` is simply packaging a
series of extras on the `Intent`, so you can always put those extras
on yourself if you so choose. The following table lists the available
configuration methods on `CameraActivity.IntentBuilder`, the corresponding
extra names (defined as constants on `CameraActivity`), their default values,
and what their behavior is:

| `IntentBuilder` Method     | Extra Key                        | Data Type                                 | Purpose |
|:--------------------------:|:--------------------------------:|:-----------------------------------------:|---------|
| `debug()`                  | `EXTRA_DEBUG_ENABLED`            | `boolean`                                 | Indicate if extra debugging information should be dumped to LogCat (default is `false`) |
| `facing()`                 | `EXTRA_FACING`                   | `AbstractCameraActivity.Facing`           | Indicate the preferred camera to start with (`BACK` or `FRONT`, default is `BACK`) |
| `forceClassic()`           | `EXTRA_FORCE_CLASSIC`            | `boolean`                                 | Indicate if the `Camera` API should be used on Android 5.0+ devices instead of `camera2` (default is `false`) |
| `skipConfirm()`            | `EXTRA_CONFIRM`                  | `boolean`                                 | Indicate if the user should be presented with a preview of the image and needs to accept it before proceeding (default is to show the confirmation screen) |
| `to()`                     | `MediaStore.EXTRA_OUTPUT`        | `Uri` (though `to()` also accepts `File`) | Destination for picture to be written, where `null` means to return a thumbnail via the `data` extra (default is `null`) |
| `updateMediaStore()`       | `EXTRA_UPDATE_MEDIA_STORE`       | `boolean`                                 | Indicate if `MediaStore` should be notified about newly-captured photo (default is `false`)|
| `mirrorPreview()`          | `EXTRA_MIRROR_PREVIEW`           | `boolean`                                 | Indicate if preview should be horizontally flipped (default is `false`)|
| `focusMode()`              | `EXTRA_FOCUS_MODE`               | `FocusMode`                               | Indicate the desired focus mode for the camera (default is continuous if available, else device default) |
| `debugSavePreviewFrame()`  | `EXTRA_DEBUG_SAVE_PREVIEW_FRAME` | `boolean`                                 | Indicate if a preview frame should be saved when a picture is taken (default is `false`) |
| `flashModes()`             | `EXTRA_FLASH_MODES`              | `List<FlashMode>`                         | Request a particular flash mode `FlashMode.OFF`, `FlashMode.ALWAYS`, `FlashMode.AUTO`, `FlashMode.REDYE` (default is device default) |
| `zoomStyle()`              | `EXTRA_ZOOM_STYLE`               | `ZoomStyle`                               | Request to allow the user to change zoom levels, via gestures (`ZoomStyle.PINCH`) or a `SeekBar` (`ZoomStyle.SEEKBAR`). Default is `ZoomStyle.NONE` for no zoom option |

Note that if you are going to use `skipConfirm()`, you need to call
that first on the `IntentBuilder` before any of the others.
This limitation will be lifted (hopefully) [in the future](https://github.com/commonsguy/cwac-cam2/issues/69).

Also note that `mirrorPreview()` mirrors the preview based on the
orientation when the activity instance was created. Since the
library will recreate the activity on a configuration change,
things look "normal" when the device is not being actively rotated.
However, when the device is part-way through the rotation, before
the configuration change kicks in, the mirroring effect starts
becoming more of a vertical flip rather than a horizontal one.
In short: the image will look upside-down briefly.

For flash support, you have:

- `flashMode()`, which takes a single flash mode
- `flashModes()`, which takes either `FlashMode[]` or `List<FlashMode>`

If you supply more than one flash mode (via `flashModes()`), they
will be tried in the order you supply. So, the first mode will be
used if it is supported, otherwise the second mode will be used, etc.
If no mode you request is supported, whatever the default device
behavior is will be performed, which is usually no flash.

## Example Use of `IntentBuilder`

```java
  Intent i=new CameraActivity.IntentBuilder(MainActivity.this)
      .skipConfirm()
      .facing(CameraActivity.Facing.FRONT)
      .to(new File(testRoot, "portrait-front.jpg"))
      .debug()
      .zoomStyle(ZoomStyle.SEEKBAR)
      .updateMediaStore()
      .build();

  startActivityForResult(i, REQUEST_PORTRAIT_FFC);
```

## `buildChooser()`

In addition to `build()`, `IntentBuilder` supports `buildChooser()`.
This will return an `Intent` that will bring up an activity chooser,
where the user can choose between this library's camera activity
or existing `ACTION_IMAGE_CAPTURE` implementations. This way, the
user gets the choice of what should be used to take the picture.

`buildChooser()` takes a `CharSequence` parameter, for a title
to go over the chooser dialog. `null` means do not use a title.

## Output

If you provide the destination `Uri` via `to()`, the image will be written there, and the `Uri` of the `Intent`
delivered to `onActivityResult()` will be your requested `Uri`.

If you do not provide the destination `Uri`, a thumbnail image will be supplied via the `data` extra on the `Intent` delivered to `onActivityResult()`.

And, of course, the `resultCode` passed to `onActivityResult()` will indicate if the user took a picture or abandoned the operation.

## Configuring the Manifest Entry

Getting all of the above working requires nothing in your manifest.
However, more often than not, you will want to change aspects of the
activity, such as its theme.

To do that, add your own `<activity>` element to the manifest, pointing
to the `CameraActivity` class, and add in whatever attributes or child
elements that you need.

For example, the following manifest entry sets the theme:

```xml
<activity
      android:name="com.commonsware.cwac.cam2.CameraActivity"
      android:theme="@style/AppTheme"/>
```

Note that `CameraActivity` does not support being exported. Do not add
an `<intent-filter>` to this activity or otherwise mark it as being
exported.

Also note that `CameraActivity` needs a theme with the native action
bar, unless you use `skipConfirm()`, in which case `CameraActivity`
should be able to work without an action bar.

`CameraActivity` supports running in a separate process, via
the `android:process` attribute. This ensures that the heap space
consumed in all the camera processing will not affect your main
process' heap space. It does mean that you will consume more system
RAM while the user is taking a picture, and it does incrementally
slow down the launching of the `CameraActivity`. You can see this
use of `android:process` demonstrated in the `demo-playground/`
sample project.

## Permissions

This library uses certain permissions. You may wish to review
[the documentation on permissions](Permissions.md) to learn
more about what they are and how to manage them.
