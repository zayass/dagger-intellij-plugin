package com.squareup.ideaplugin.dagger;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.squareup.ideaplugin.dagger.handler.ParameterInjectToProvidesHandler;
import com.squareup.ideaplugin.dagger.handler.FieldInjectToProvidesHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

import static com.intellij.codeHighlighting.Pass.UPDATE_ALL;
import static com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment.LEFT;
import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_INJECT;
import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_PROVIDES;
import static com.squareup.ideaplugin.dagger.PsiConsultantImpl.hasAnnotation;

public class InjectionLineMarkerProvider implements LineMarkerProvider {
  private static final Icon ICON = IconLoader.getIcon("/icons/inject.png");

  /**
   * Check the element. If the element is a PsiMethod, than we want to know if it's a Constructor
   * annotated w/ @Inject.
   *
   * If element is a field, than we only want to see if it is annotated with @Inject.
   *
   * @return a {@link com.intellij.codeInsight.daemon.GutterIconNavigationHandler} for the
   *         appropriate type, or null if we don't care about it.
   */
  @Nullable @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
    // Check methods first (includes constructors).
    if (element instanceof PsiMethod) {
      PsiMethod methodElement = (PsiMethod) element;

      // Constructor injection.
      boolean isInjectableConstructor = methodElement.isConstructor() && PsiConsultantImpl.hasAnnotation(element, CLASS_INJECT);
      // injection to provides method
      boolean isProvidesMethod = hasAnnotation(element, CLASS_PROVIDES);



      if (isInjectableConstructor || isProvidesMethod) {
        PsiParameterList parameterList = methodElement.getParameterList();

        if (parameterList.getParametersCount() > 0) {
          return new LineMarkerInfo<PsiElement>(element, parameterList.getTextRange(), ICON,
              UPDATE_ALL, null, new ParameterInjectToProvidesHandler(), LEFT);
        }
      }

      // Not a method, is it a Field?
    } else if (element instanceof PsiField) {
      // Field injection.
      PsiField fieldElement = (PsiField) element;
      PsiTypeElement typeElement = fieldElement.getTypeElement();

      if (PsiConsultantImpl.hasAnnotation(element, CLASS_INJECT) && typeElement != null) {
        return new LineMarkerInfo<PsiElement>(element, typeElement.getTextRange(), ICON, UPDATE_ALL,
            null, new FieldInjectToProvidesHandler(), LEFT);
      }
    }

    return null;
  }

  @Override public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements,
      @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {
    // Sure buddy. You ever explain how and we just might.
  }
}
