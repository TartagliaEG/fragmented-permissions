package br.com.tartagliaeg.fragmented.permissions.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

/**
 * Created by tartaglia on 11/25/17.
 * ...
 */
class PermissionStore implements IPermission.Store {
  private SharedPreferences mPreferences;
  private Context mContext;
  private final String PREFIX;
  private static final String TAG = PermissionStore.class.getName();

  static final String SP_NAME = TAG + ".PERMISSIONS_SHARED_PREFERENCES";
  static final String SP_PERMISSION_GRANTED = TAG + ".PERMISSION_GRANTED.";
  static final String SP_PERMISSION_ASKED = TAG + ".PERMISSION_ASKED.";
  static final String SP_PERMISSION_NOT_ASK_AGAIN = TAG + ".PERMISSION_NOT_ASK_AGAIN.";
  private boolean mStarted = false;

  public PermissionStore(String prefix) {
    this.PREFIX = prefix;
  }

  void create(Context context) {
    this.mContext = context.getApplicationContext();
    this.mPreferences = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    this.mStarted = true;
  }

  void destroy() {
    assertValidState();
    mContext = null;
    mPreferences = null;
    mStarted = false;
  }

  @Override
  public void savePermission(Permission perm) {
    assertValidState();
    String name = perm.getPermissionName();

    this.mPreferences
      .edit()
      .putBoolean(SP_PERMISSION_NOT_ASK_AGAIN + name, perm.isNotAskAgain())
      .putBoolean(SP_PERMISSION_GRANTED + name, perm.isGranted())
      .putBoolean(SP_PERMISSION_ASKED + name, perm.isAsked())
      .apply();
  }

  @SuppressWarnings("SimplifiableConditionalExpression")
  public boolean isPermissionAsked(String permissionName) {
    assertValidState();
    return isRuntimePermissionSupported()
      ? this.mPreferences.getBoolean(SP_PERMISSION_ASKED + permissionName, false)
      : true;
  }

  @SuppressWarnings("SimplifiableConditionalExpression")
  public boolean isPermissionNotAskAgain(String permissionName) {
    assertValidState();
    return isRuntimePermissionSupported()
      ? this.mPreferences.getBoolean(SP_PERMISSION_NOT_ASK_AGAIN + permissionName, false)
      : false;
  }

  @SuppressWarnings("SimplifiableConditionalExpression")
  public boolean isPermissionGranted(String permissionName) {
    assertValidState();
    return isRuntimePermissionSupported()
      ? this.mContext.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED
      : true;
  }

  private boolean isRuntimePermissionSupported() {
    return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
  }

  private void assertValidState() {
    if (!mStarted)
      throw new IllegalStateException("Can't call PermissionStore methods before create was called");
  }

}
