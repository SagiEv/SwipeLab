import { ImageSourcePropType, StyleProp, ViewStyle } from "react-native";

export type ScreenHeaderLayoutProps = {
  leftIcon: ImageSourcePropType;
  leftTitle: string;

  rightIcon: ImageSourcePropType;
  rightTitle: string;
  onRightPress?: () => void;
  contentContainerStyle?: StyleProp<ViewStyle>;

  children: React.ReactNode;
};
