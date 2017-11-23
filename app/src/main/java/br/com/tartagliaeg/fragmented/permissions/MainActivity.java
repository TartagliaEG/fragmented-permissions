package br.com.tartagliaeg.fragmented.permissions;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import br.com.tartagliaeg.fragmented.permissions.core.IPermissionManager;
import br.com.tartagliaeg.fragmented.permissions.core.IPermissionStore;
import br.com.tartagliaeg.fragmented.permissions.core.PermissionManager;
import br.com.tartagliaeg.fragmented.permissions.utils.SimpleObserver;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getName();
  private IPermissionManager mPermissions;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mPermissions = new PermissionManager();

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(mCoordinatesClickListener);
    fab.setOnLongClickListener(mCoordinatesLongClickListener);

    getSupportFragmentManager()
        .beginTransaction()
        .add((Fragment) mPermissions, PermissionManager.FRAGMENT_TAG)
        .commit();
  }

  private View.OnClickListener mCoordinatesClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      mPermissions
          .askForNotAskedPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
          .subscribe(new SimpleObserver<IPermissionStore>() {
            @Override
            public void onNext(@NonNull IPermissionStore iPermissionStore) {
              Log.i(TAG, iPermissionStore.retrievePermission(Manifest.permission.ACCESS_FINE_LOCATION).toString());
            }
          });

    }
  };

  private View.OnLongClickListener mCoordinatesLongClickListener = new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View v) {

      mPermissions
          .askForPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
          .subscribe(new SimpleObserver<IPermissionStore>() {
            @Override
            public void onNext(@NonNull IPermissionStore iPermissionStore) {
              Log.i(TAG, iPermissionStore.retrievePermission(Manifest.permission.ACCESS_FINE_LOCATION).toString());
            }
          });
      return false;
    }
  };
}
