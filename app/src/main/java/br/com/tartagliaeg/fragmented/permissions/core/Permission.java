package br.com.tartagliaeg.fragmented.permissions.core;

/**
 * Created by tartagle on 23/11/2017.
 * ...
 */
@SuppressWarnings("WeakerAccess")
public class Permission {
  private final String mPermission;
  private final boolean mGranted;
  private final boolean mNotAskAgain;
  private final boolean mAsked;

  @Override
  public String toString() {
    return "Permission { " +
        "mPermission: " + mPermission +
        ", mGranted: " + mGranted +
        ", mNotAskAgain: " + mNotAskAgain +
        ", mAsked: " + mAsked +
        " }";
  }

  /**
   * @param permission  - Permission name
   * @param granted     - If the permission was granted
   * @param asked       - If the permission was already requested
   * @param notAskAgain - If the user check the "not ask again" option
   */
  Permission(String permission, boolean granted, boolean asked, boolean notAskAgain) {
    mPermission = permission;
    mGranted = granted;
    mNotAskAgain = notAskAgain;
    mAsked = asked;
  }

  public String getPermissionName() {
    return mPermission;
  }

  public boolean isGranted() {
    return mGranted;
  }

  public boolean isNotAskAgain() {
    return mNotAskAgain;
  }

  public boolean isAsked() {
    return mAsked;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Permission that = (Permission) o;

    return mPermission.equals(that.mPermission);

  }

  @Override
  public int hashCode() {
    return mPermission.hashCode();
  }
}
