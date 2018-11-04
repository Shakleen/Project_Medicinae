package Logic;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class C_MainWindow {
    @FXML private ImageView FX_ImageView_InsertInfo;
    @FXML private ImageView FX_ImageView_SyncInfo;
    @FXML private ImageView FX_ImageView_ViewInfo;
    @FXML private ImageView FX_ImageView_Help;
    @FXML private ImageView FX_ImageView_Account;
    @FXML private ImageView FX_ImageView_Exit;

    public void initialize(){
        SetUpImages(75, 100, true);
    }


    /**
     * A function used to set-up images.
     * @param Height the height of the images
     * @param Width the width of the images
     * @param PreserveRatio whether to preserve the ratio of the images
     */
    public void SetUpImages(double Height, double Width, boolean PreserveRatio){
        FX_ImageView_InsertInfo.setImage(new Image("/Images/Insert_Info.png"));
        FX_ImageView_InsertInfo.setFitHeight(Height);
        FX_ImageView_InsertInfo.setFitWidth(Width);
        FX_ImageView_InsertInfo.setPreserveRatio(PreserveRatio);

        FX_ImageView_SyncInfo.setImage(new Image("/Images/Sync_Info.png"));
        FX_ImageView_SyncInfo.setFitHeight(Height);
        FX_ImageView_SyncInfo.setFitWidth(Width);
        FX_ImageView_SyncInfo.setPreserveRatio(PreserveRatio);

        FX_ImageView_ViewInfo.setImage(new Image("/Images/View_Info.png"));
        FX_ImageView_ViewInfo.setFitHeight(Height);
        FX_ImageView_ViewInfo.setFitWidth(Width);
        FX_ImageView_ViewInfo.setPreserveRatio(PreserveRatio);


        FX_ImageView_Help.setImage(new Image("/Images/Help.png"));
        FX_ImageView_Help.setFitHeight(Height);
        FX_ImageView_Help.setFitWidth(Width);
        FX_ImageView_Help.setPreserveRatio(PreserveRatio);

        FX_ImageView_Account.setImage(new Image("/Images/Account.png"));
        FX_ImageView_Account.setFitHeight(Height);
        FX_ImageView_Account.setFitWidth(Width);
        FX_ImageView_Account.setPreserveRatio(PreserveRatio);

        FX_ImageView_Exit.setImage(new Image("/Images/Exit.png"));
        FX_ImageView_Exit.setFitHeight(Height);
        FX_ImageView_Exit.setFitWidth(Width);
        FX_ImageView_Exit.setPreserveRatio(PreserveRatio);
    }
}
