# glovo-challenge-mobile

### Data flows screencast

![image](https://github.com/tolmachevroman/glovo-challenge-mobile/blob/master/assets/glovo-test.gif)

### Notes and reflections

#### Testing on a device

- I used my local machine's network address to test from device directly (be sure to [update url in build.gradle](https://github.com/tolmachevroman/glovo-challenge-mobile/blob/master/client/app/build.gradle#L19)  therefore)

- Although not specified by the task, I added **Select city** menu, so that user could always browse through cities

#### Architecture

- I like MVVM + Kotlin + Dagger 2 stack, also found it easy to combine `RxJava` with `LiveData`  

- Preferred not to add `Room` database to repositories since there's no sync logic and to focus on map details instead 

#### Tests

-  Added a sync plain JUnit test of [building ordered items list method](https://github.com/tolmachevroman/glovo-challenge-mobile/blob/master/client/app/src/test/java/com/glovo/test/SelectCityViewModelTest.kt#L41)

- That said, it's not straightforward how to implement elaborated tests for `MutableLiveData` or `RxJava` code. Found https://medium.com/@fabioCollini/testing-asynchronous-rxjava-code-using-mockito-8ad831a16877 that I want to investigate later

- Recording Espresso test scripts from Android Studio works until you need to deal with async data, want to investigate https://developer.android.com/training/testing/espresso/idling-resource
