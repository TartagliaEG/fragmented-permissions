package br.com.tartagliaeg.fragmented.permissions.core;

import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by tartaglia on 11/25/17.
 * ...
 */

@SuppressWarnings("WeakerAccess")
public class PermissionManager implements IPermission.Manager {
  @SuppressWarnings("unused")
  private static final String TAG = PermissionManager.class.getName();
  private boolean mStarted = false;

  private IPermission.Store mStore;
  private IPermission.Fragment mFragment;
  private StashedProperties mProps;

  private PublishSubject<IPermission.Retriever> mPermissionsPublisher;

  public void start(@NonNull IPermission.Fragment fragment, @NonNull IPermission.Store store, @NonNull StashedProperties props) {
    mFragment = fragment;
    mStore = store;
    mPermissionsPublisher = PublishSubject.create();
    mProps = props;
    mStarted = true;
  }

  public void stop() {
    assertValidState();
    mPermissionsPublisher.onComplete();
    mPermissionsPublisher = null;
    mFragment = null;
    mStore = null;
    mProps = null;
    mStarted = false;
  }

  public void handlePermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
    assertValidState();

    if (!mProps.mIsWaitingPermissionDialogResponse)
      throw new IllegalStateException("Should not call handlePermissionResult without calling askForPermissions first.");

    for (int i = 0; i < permissions.length; i++) {
      int grantedResult = grantResults[i];
      String permission = permissions[i];

      boolean notAskAgain = false;
      boolean granted = grantedResult == PackageManager.PERMISSION_GRANTED;

      if (!granted) {
        notAskAgain = !mFragment.canAskPermissionAgain(permission);
      }

      Permission perm = new Permission(permission, granted, true, notAskAgain);
      mStore.savePermission(perm);
    }

    mProps.mIsWaitingPermissionDialogResponse = false;
    mPermissionsPublisher.onNext(this);
    mPermissionsPublisher.onComplete();
    mPermissionsPublisher = PublishSubject.create();
  }

  /**
   * Ask the given permissions to Android. It will prompt the user to give the requested permissions.
   *
   * @param permissions - A list of permission names. The permission names can be found on Manifest.permission.*
   * @return An observable that will be called when the permissions request receives a result.
   */
  @Override
  @NonNull
  public Observable<IPermission.Retriever> askForPermissions(String... permissions) {
    assertValidState();
    if (mProps.mIsWaitingPermissionDialogResponse)
      return mPermissionsPublisher;

    boolean isAllGranted = true;

    for (int i = 0; i < permissions.length && isAllGranted; i++)
      isAllGranted = mStore.isPermissionGranted(permissions[i]);

    if (isAllGranted)
      return Observable.just((IPermission.Retriever) this);

    mProps.mIsWaitingPermissionDialogResponse = true;
    mFragment.showPermissionDialog(permissions);

    return mPermissionsPublisher;
  }

  @Override
  @NonNull
  public Observable<IPermission.Retriever> askForNotAskedPermissions(String... permissions) {
    assertValidState();
    if (mProps.mIsWaitingPermissionDialogResponse)
      return mPermissionsPublisher;

    List<String> notAsked = new ArrayList<>();
    boolean isAllGranted = true;

    for (int i = 0; i < permissions.length; i++) {
      String permission = permissions[i];

      boolean granted = mStore.isPermissionGranted(permission);
      boolean asked = mStore.isPermissionAsked(permission);
      isAllGranted = granted && isAllGranted;

      if (!granted && !asked)
        notAsked.add(permission);
    }

    if (isAllGranted || notAsked.size() == 0)
      return Observable.just((IPermission.Retriever) this);

    return askForPermissions(notAsked.toArray(new String[notAsked.size()]));
  }

  @Override
  public Permission retrievePermission(String permissionName) {
    assertValidState();
    return new Permission(
      permissionName,
      mStore.isPermissionGranted(permissionName),
      mStore.isPermissionAsked(permissionName),
      mStore.isPermissionNotAskAgain(permissionName)
    );
  }

  private void assertValidState() {
    if (!mStarted)
      throw new IllegalStateException("Can't call PermissionManager methods before start method was called.");
  }

  public static class StashedProperties implements Parcelable {
    boolean mIsWaitingPermissionDialogResponse;

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeByte(this.mIsWaitingPermissionDialogResponse ? (byte) 1 : (byte) 0);
    }

    public StashedProperties() {
    }

    protected StashedProperties(Parcel in) {
      this.mIsWaitingPermissionDialogResponse = in.readByte() != 0;
    }

    public static final Parcelable.Creator<StashedProperties> CREATOR = new Parcelable.Creator<StashedProperties>() {
      @Override
      public StashedProperties createFromParcel(Parcel source) {
        return new StashedProperties(source);
      }

      @Override
      public StashedProperties[] newArray(int size) {
        return new StashedProperties[size];
      }
    };
  }
}

