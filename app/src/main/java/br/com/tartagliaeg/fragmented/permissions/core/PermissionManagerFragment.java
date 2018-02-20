package br.com.tartagliaeg.fragmented.permissions.core;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import io.reactivex.Observable;

/**
 * Created by tartaglia on 23/11/2017.
 * ...
 */
public class PermissionManagerFragment extends Fragment implements IPermission.Requester, IPermission.Fragment, IPermission.Retriever {
  private static final String TAG = PermissionManagerFragment.class.getName();
  public static final String FRAGMENT_TAG = "Fragment:" + TAG;
  private static final String ARG_SCOPE = TAG + ".SCOPE";
  private static final String DEFAULT_SCOPE = "DEFAULT_SCOPE";
  private static final int RC_ASK_FOR_PERMISSION = 27401;
  private static final String SS_MANAGER_PROPERTIES = TAG + ".MANAGER_PROPERTIES";

  private PermissionManager mManager;
  private PermissionManager.StashedProperties mProps;
  private PermissionStore mStore;

  public static IPermission.Requester getPermissionRequester(FragmentManager fm) {
    return (IPermission.Requester) fm.findFragmentByTag(FRAGMENT_TAG);
  }

  /**
   * Creates a properly configured instance of this fragment.
   *
   * @return A properly configured instance of PermissionManagerFragment
   */
  public static Fragment newInstance() {
    return newInstance(DEFAULT_SCOPE);
  }

  /**
   * Creates a properly configured instance of this fragment.
   *
   * @param scope The scope used to store permissions. Providing an scope allows the permission management to be made per scope.
   * @return A properly configured instance of PermissionManagerFragment
   */
  public static Fragment newInstance(String scope) {
    Fragment fragment = new PermissionManagerFragment();
    Bundle bundle = new Bundle();
    bundle.putString(ARG_SCOPE, scope);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    String scope = getArguments() != null ? getArguments().getString(ARG_SCOPE) : DEFAULT_SCOPE;

    mManager = new PermissionManager();
    mProps = new PermissionManager.StashedProperties();
    mStore = new PermissionStore(scope);

    if (savedInstanceState != null)
      mProps = savedInstanceState.getParcelable(SS_MANAGER_PROPERTIES);

    super.onCreate(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    mStore.start(getContext());
    mManager.start(this, mStore, mProps);
  }

  @Override
  public void onStop() {
    super.onStop();
    mManager.stop();
    mStore.stop();
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    outState.putParcelable(SS_MANAGER_PROPERTIES, mProps);
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode != RC_ASK_FOR_PERMISSION)
      return;

    mManager.handlePermissionResult(permissions, grantResults);
  }

  /**
   * Ask the given permissions to Android. It will prompt the user to give the requested permissions.
   *
   * @param permissions - A list of permission names. The permission names can be found on Manifest.permission.*
   * @return An observable that will be called when the permissions request receives a result.
   */
  @Override
  public Observable<IPermission.Retriever> askForPermissions(String... permissions) {
    return mManager.askForPermissions(permissions);
  }

  @Override
  public Observable<IPermission.Retriever> askForNotAskedPermissions(String... permissions) {
    return mManager.askForNotAskedPermissions(permissions);
  }

  @Override
  public boolean canAskPermissionAgain(String permissionName) {
    return this.shouldShowRequestPermissionRationale(permissionName);
  }

  @Override
  public void showPermissionDialog(String[] permissions) {
    this.requestPermissions(permissions, RC_ASK_FOR_PERMISSION);
  }

  @Override
  public Permission retrievePermission(String permissionName) {
    return mManager.retrievePermission(permissionName);
  }
}
