package com.redbeemedia.enigma.referenceapp.databinding;
import com.redbeemedia.enigma.referenceapp.R;
import com.redbeemedia.enigma.referenceapp.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class TvExoplayerControlsBindingImpl extends TvExoplayerControlsBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.subtitles_spinner, 1);
        sViewsWithIds.put(R.id.rewind_button, 2);
        sViewsWithIds.put(R.id.play_pause_container, 3);
        sViewsWithIds.put(R.id.play_button, 4);
        sViewsWithIds.put(R.id.pause_button, 5);
        sViewsWithIds.put(R.id.forward_button, 6);
        sViewsWithIds.put(R.id.timelineView, 7);
    }
    // views
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public TvExoplayerControlsBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 8, sIncludes, sViewsWithIds));
    }
    private TvExoplayerControlsBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.ImageView) bindings[6]
            , (androidx.constraintlayout.widget.ConstraintLayout) bindings[0]
            , (android.widget.ImageView) bindings[5]
            , (android.widget.ImageView) bindings[4]
            , (android.widget.FrameLayout) bindings[3]
            , (android.widget.ImageView) bindings[2]
            , (com.redbeemedia.enigma.referenceapp.ui.SubtitleTracksSpinner) bindings[1]
            , (com.redbeemedia.enigma.referenceapp.ui.TimelineView) bindings[7]
            );
        this.outerControlsContainer.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        // batch finished
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): null
    flag mapping end*/
    //end
}