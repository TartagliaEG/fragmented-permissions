# fragmented-permissions

Library for handling the whole permission flow in a reactive way. 

#### Configuration
Before start to request permissions you should get an instance of IPermission.Requester. 
The PermissionManagerFragment class is the main component here, it implements the required interface.
As the name implies, the PermissionManagerFragment inherits from fragment, so it can be instanced programmatically (via its factory method #newInstance) or by adding it on the current layout (through the \<fragment> tag).

The fragment instance must exists into the FragmentManager to work properly. If you choose to use the \<fragment> tag, it is done automatically for you. If you created an instance programmatically, you must add it manually.

**Getting an instance programmatically**
```java
FragmentManager fm = getSupportFragmentManager();
 
mPermissions = (IPermission.Requester) fm.findFragmentByTag(PermissionManagerFragment.FRAGMENT_TAG);
 
if(mPermissions == null) {
  mPermissions = (IPermission.Requester) PermissionManagerFragment.newInstance();
  fm.beginTransaction()
    .add((Fragment) mPermissions, PermissionManagerFragment.FRAGMENT_TAG)
    .commit();
}
```

**Getting an instance through xml layout**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  android:orientation="vertical" >
    <fragment 
      android:name="br.com.tartagliaeg.fragmented.permissions.PermissionManagerFragment"
      android:id="@+id/permission_fragment" />
</LinearLayout>
```

```java
mPermissions = (IPermission.Requester) getSupportFragmentManager().findFragmentById(R.id.permission_fragment);
```



#### Permission Request Flow
Once you have an instance of IPermission.Requester you can use the methods #askForPermission and #askForNotAskedPermission. 
These methods will return a Single<T> which provides an instance of IPermission.Retriever as parameter. The last can be used to get Permission instances.


**Permission Request Flow Example:**
```java
public class MyActivity extends AppCompatActivity {
  IPermission.Requester mPermissions;
  
  // Configuring the PermissionManager
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // [...] //
    
    mPermissions = (IPermission.Requester) PermissionManagerFragment.newInstance();

    getSupportFragmentManager()
      .beginTransaction()
      .add((Fragment) mPermissions, PermissionManagerFragment.FRAGMENT_TAG)
      .commit();
    
    // [...] //
  }
 
  
  // Using the permission manager
  public void myMethodWhichRequiresLocationPermission() {
    mPermissions
      .askForNotAskedPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
      // (or) .askForPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
      .map(new Function<IPermission.Retriever, IPermission>() {
        @Override
        public IPermission apply(IPermission.Retriever retriever) throws Exception {
          return retriever.retrievePermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }
      })
      .filter(new Predicate<IPermission>() {
        @Override
        public boolean test(IPermission iPermission) throws Exception {
          return iPermission.isGranted();
        }
      })
      .subscribe(new SimpleObserver<IPermission>() {
        @Override
        public void onNext(IPermission iPermission) {
          // Run the code which depends on the asked permission
          IPermission permission = retriever.retrievePermission(Manifest.permission.ACCESS_FINE_LOCATION);
        
          permission.getPermissionName();
          permission.isGranted();
          permission.isNotAskAgain();
          permission.isAsked();

        }
      });
  }
}
```