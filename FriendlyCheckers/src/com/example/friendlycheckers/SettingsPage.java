using System;
using System.Windows;
using System.Windows.Controls;
using Microsoft.Phone.Controls;

namespace FriendlyCheckers
{
    public partial class SettingsPage : PhoneApplicationPage
    {
        public SettingsPage()
        {
            InitializeComponent();
            Op_ForceJump.IsChecked = MainPage.FORCE_JUMP;
            Op_DiffHard.IsChecked = MainPage.DIFFICULT;
            Op_DiffEasy.IsChecked = !Op_DiffHard.IsChecked;

           // MessageBox.Show(MainPage.DIFFICULT+", "+MainPage.FORCE_JUMP);
        }
        private void CheckBox_Checked(object sender, RoutedEventArgs e)
        {
            if (sender.Equals(Op_ForceJump))
            {
                MainPage.FORCE_JUMP = (Op_ForceJump.IsChecked == true);
                return;
            }
            if(sender.Equals(Op_DiffHard))
                MainPage.DIFFICULT = (Op_DiffHard.IsChecked == true);
            else
                MainPage.DIFFICULT = (Op_DiffEasy.IsChecked == false);
             Op_DiffEasy.IsChecked = !MainPage.DIFFICULT;
             Op_DiffHard.IsChecked = MainPage.DIFFICULT;
            // MessageBox.Show("DIFFICULT SET TO: "+MainPage.DIFFICULT + ", FORCE JUMP SET TO: " + MainPage.FORCE_JUMP);
        }
    }
}